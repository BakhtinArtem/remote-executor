package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Node;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class RunnerService {

    @Value("${JAR_FILES_PATH}")
    private String JAR_FILES_PATH;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private final Logger logger = LoggerFactory.getLogger(RunnerService.class);

    public void runTask(Execution execution, Node node) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(configBuilder.build()).build()) {
            final String namespace = "default";
            final Job job = new JobBuilder()
//                    todo: mount should be specified
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                        .withName(String.valueOf(node.getId()))
//                        .withLabels(Collections.singletonMap("label1", "maximum-length-of-63-characters"))
//                        .withAnnotations(Collections.singletonMap("annotation1", "some-very-long-annotation"))
                    .endMetadata()
                    .withNewSpec()
                        .withNewTemplate()
                            .withNewSpec()
//                                .withServiceAccountName("job-manager")
                                .addNewContainer()
                                    .withName(String.valueOf(node.getId()))
                                    .withImage(node.getImage())
                                    .withArgs("java", "-jar", JAR_FILES_PATH + "/" + node.getFilename())
                                    .withVolumeMounts(new VolumeMountBuilder()
                                            .withMountPath("/jars")
                                            .withName("jars-storage")
                                            .build()
                                    )
                                .endContainer()
                                .withVolumes(new VolumeBuilder()
                                        .withName("jars-storage")
                                        .withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder()
                                                .withClaimName("local-pvc")
                                                .build()
                                        )
                                        .build()
                                )
                                .withRestartPolicy("Never")
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

            logger.info(String.format("Creating job for %s.", node.getFilename()));
//            creating job
            client.batch().v1().jobs().inNamespace(namespace).resource(job).create();
//            get all pods created by the job
            PodList podList = client.pods().inNamespace(namespace).withLabel("job-name", String.valueOf(node.getId())).list();
            logger.info(String.format("Job name %s.", podList.getItems().get(0).getMetadata().getName()));
//            fixme: watch is not receiving actions
            client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(Action action, Pod resource) {
                            logger.info("received action " + action);
                            String joblog = client.batch().v1().jobs().inNamespace(namespace)
                                    .withName(String.valueOf(node.getId())).getLog();
                            logger.info(joblog);
                        }

                        @Override
                        public void onClose(WatcherException cause) {
                            logger.info(cause.getMessage());
                        }
                    });

            // Print Job's log
//            String joblog = client.batch().v1().jobs().inNamespace(namespace).withName("pi").getLog();
//            logger.info(joblog);

        } catch (KubernetesClientException e) {
            logger.error("Unable to create job", e);
        }
    }
}
