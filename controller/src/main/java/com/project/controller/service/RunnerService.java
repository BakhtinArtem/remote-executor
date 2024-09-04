package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Node;
import com.project.controller.event.NodeExecutedEvent;
import com.project.controller.model.ExecutionStatusEnum;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class RunnerService {

    @Value("${JAR_FILES_PATH}")
    private String JAR_FILES_PATH;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private final Logger logger = LoggerFactory.getLogger(RunnerService.class);

    /**
     * Run JAR file as kubernetes job
     * @param execution - current execution
     * @param node - node to execute
     * @return 0 if job successfully finished and 0 otherwise
     */
    @Async(value = "threadPoolTaskExecutor")
    public CompletableFuture<ExecutionStatusEnum> runTask(Execution execution, Node node) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        final String namespace = "default";
        final String name = execution.getId() + "-" + node.getId();
//        return value that indicates whether the job is successfully completed or not
        var isSucceeded = ExecutionStatusEnum.FAILED;
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(configBuilder.build()).build()) {
//            job definition
            final Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                        .withName(name)
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
//                    todo: mount should be hardcoded? - probably not, but should be synchronized with yaml config somehow ...
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
//            creating the job
            client.batch().v1().jobs().inNamespace(namespace).resource(job).create();
//            waiting for the job readiness (1 minute max)
            client.batch().v1().jobs().inNamespace(namespace).withName(job.getMetadata().getName())
                    .waitUntilReady(1, TimeUnit.MINUTES);
//            waiting for the successful job completion (5 minute max) and return if succeeded
            final var resultJob = client.batch().v1().jobs().inNamespace(namespace).withName(job.getMetadata().getName())
                    .waitUntilCondition(pod -> pod.getStatus().getSucceeded() != null &&
                                    pod.getStatus().getSucceeded() == 1, 1, TimeUnit.MINUTES);
//            update status
            isSucceeded = resultJob.getStatus().getSucceeded() != null && resultJob.getStatus().getSucceeded() == 1 ?
                    ExecutionStatusEnum.SUCCEEDED : ExecutionStatusEnum.FAILED;
        } catch (KubernetesClientException e) {
            logger.error("Unable to create job", e);
        } finally {
//            cleaning resources by deleting recently created job
            try (KubernetesClient client = new KubernetesClientBuilder().withConfig(configBuilder.build()).build()) {
                final CountDownLatch deleteLatch = new CountDownLatch(1);
                final CountDownLatch closeLatch = new CountDownLatch(1);
                client.batch().v1().jobs().inNamespace(namespace).withName(name).watch(new Watcher<Job>() {
                    @Override
                    public void eventReceived(Action action, Job resource) {
                        logger.warn("Job " + name + " was " + action);
                        switch (action) {
                            case DELETED -> deleteLatch.countDown();
                            default -> {}
                        }
                    }

                    @Override
                    public void onClose(WatcherException cause) { closeLatch.countDown(); }
                });
    //            delete completed job
                client.batch().v1().jobs().inNamespace(namespace).withName(name).delete();
    //            waiting for deletion
                final var isDelete = deleteLatch.await(1, TimeUnit.SECONDS);
                final var isWatchClosed = closeLatch.await(1, TimeUnit.MINUTES);
                logger.info("Job is" + (isDelete ? " " : " NOT ") + "deleted and watch is" + (isWatchClosed ? " " : " NOT ") + "closed");
            } catch (InterruptedException e) {
                logger.error("Unable to delete " + name + " resource");
            }
        }
//        todo: we can just call without event pusblishing?
//        publish event about executed node
        eventPublisher.publishEvent(new NodeExecutedEvent(execution, node, isSucceeded));
        logger.info("Job " + name + " " + isSucceeded);
        return CompletableFuture.completedFuture(isSucceeded);
    }

}
