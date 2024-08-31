package com.project.controller.service;

import com.project.controller.model.RunJarTaskModel;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class RunnerService {

    private final Logger logger = LoggerFactory.getLogger(RunnerService.class);

    public void runTask(RunJarTaskModel runJarTaskModel, String jarsFilesPath) throws IOException {
        final ConfigBuilder configBuilder = new ConfigBuilder();
//        todo: caching?
//        todo: saving and running should be separated
//        todo: graph processor full of this jobs
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(configBuilder.build()).build()) {
            final String namespace = "default";
            final Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                        .withName("pi")
                        .withLabels(Collections.singletonMap("label1", "maximum-length-of-63-characters"))
                        .withAnnotations(Collections.singletonMap("annotation1", "some-very-long-annotation"))
                    .endMetadata()
                    .withNewSpec()
                        .withNewTemplate()
                            .withNewSpec()
                                .withServiceAccountName("job-manager")
                                .addNewContainer()
                                    .withName("pi")
                                    .withImage("perl")
                                    .withArgs("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)")
                                .endContainer()
                                .withRestartPolicy("Never")
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

            logger.info(String.format("Creating job for %s.", runJarTaskModel.filename()));
//            creating job
            client.batch().v1().jobs().inNamespace(namespace).resource(job).create();
//            get all pods created by the job
            PodList podList = client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).list();
//            we should save job until next usage
            client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                    .waitUntilCondition(pod -> pod.getStatus().getPhase().equals("Succeeded"), 2, TimeUnit.MINUTES);

            // Print Job's log
            String joblog = client.batch().v1().jobs().inNamespace(namespace).withName("pi").getLog();
            logger.info(joblog);

        } catch (KubernetesClientException e) {
            logger.error("Unable to create job", e);
        }
    }
}
