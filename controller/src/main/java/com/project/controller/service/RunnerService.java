package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.model.ExecutionStatusEnum;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.ConfigBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RunnerService {

    @Value("${JAR_FILES_PATH}")
    private String JAR_FILES_PATH;

    @Autowired
    private ExecutionService executionService;

    @Lazy
    @Autowired
    private RunnerService self;

//    each execution associated with map of graph nodes with statuses
    private final Map<Long, ConcurrentHashMap<Long, ExecutionStatusEnum>> executionIdToRunningNodesMap =
        new ConcurrentHashMap<>();

    private final Object monitor = new Object();


    @Async(value = "threadPoolTaskExecutor")
    public void executeGraph(Graph graph, Execution execution) {
        final var nodeIdToStatuses = new ConcurrentHashMap<Long, ExecutionStatusEnum>();
        final Node[] root = new Node[1];
        graph.getNodes().forEach(node -> {
//            root node is automatically scheduled
            if (node.getIsRoot()) {
                root[0] = node;
                nodeIdToStatuses.put(node.getId(), ExecutionStatusEnum.RUNNING);
            } else {
                nodeIdToStatuses.put(node.getId(), ExecutionStatusEnum.IDLE);
            }
        });
        executionIdToRunningNodesMap.put(execution.getId(), nodeIdToStatuses);
//        async call (execution starts with the root)
        self.executeNode(execution, root[0]);

    }

    /**
     * Run JAR file as kubernetes job
     * @param execution current execution
     * @param node node to execute
     * @return status in form of ExecutionStatusEnum
     */
    @Async(value = "threadPoolTaskExecutor")
    public CompletableFuture<ExecutionStatusEnum> executeNode(Execution execution, Node node) {
        log.debug("Execution {}: execution of {} node started", execution.getId(), node.getId());
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

            log.debug("Creating job for {}.", node.getFilename());
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
            log.error("Unable to create job", e);
        } finally {
//            cleaning resources by deleting recently created job
            try (KubernetesClient client = new KubernetesClientBuilder().withConfig(configBuilder.build()).build()) {
                final CountDownLatch deleteLatch = new CountDownLatch(1);
                final CountDownLatch closeLatch = new CountDownLatch(1);
                client.batch().v1().jobs().inNamespace(namespace).withName(name).watch(new Watcher<Job>() {
                    @Override
                    public void eventReceived(Action action, Job resource) {
                        log.warn("Job {} was {}", name, action);
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
                log.debug("Job is" + (isDelete ? " " : " NOT ") + "deleted and watch is" + (isWatchClosed ? " " : " NOT ") + "closed");
            } catch (InterruptedException e) {
                log.error("Unable to delete {} resource", name);
            }
        }
//        async call to node post execution handle function
        self.handleNodeExecution(execution, node, isSucceeded);
        log.debug("Job " + name + " " + isSucceeded);
        return CompletableFuture.completedFuture(isSucceeded);
    }

    @Async(value = "threadPoolTaskExecutor")
    public void handleNodeExecution(Execution execution, Node node, ExecutionStatusEnum executionStatusEnum) {
        log.debug("Execution {}: handle {} node execution", execution.getId(), node.getId());
//        fixme: remove
        log.debug("Node incoming nodes - {} and outgoing nodes - {}", node.getIncomingNodes().size(), node.getOutgoingNodes().size());
        final var nodesIdToStatus = executionIdToRunningNodesMap.get(execution.getId());
        nodesIdToStatus.put(node.getId(), executionStatusEnum);
//        failed node execution fails graph execution
        if (executionStatusEnum.equals(ExecutionStatusEnum.FAILED)) {
            log.debug("Execution {}: failed execution for {} node", execution.getId(), node.getId());
            execution.setEndTime(LocalDateTime.now());
            execution.setStatus(ExecutionStatusEnum.FAILED);
            executionService.updateExecution(execution);
            return;
        }

//        finish execution if last node (no outgoing nodes exist) executed and others nodes executed as well
        synchronized (monitor) {
            if (node.getOutgoingNodes().isEmpty()) {
                log.debug("Execution {}: checking nodes statuses to finish this execution ...", execution.getId());
                boolean finished = true;
                for (final Map.Entry<Long, ExecutionStatusEnum> entry : nodesIdToStatus.entrySet()) {
                    log.debug("Execution {}: {} node has {} status", execution.getId(), entry.getKey(), entry.getValue());
                    if (!entry.getValue().equals(ExecutionStatusEnum.SUCCEEDED)) {
                        finished = false;
                        break;
                    }
                }
                if (finished) {
                    log.debug("Execution {}: finished", execution.getId());
                    executionService.finishExecution(execution, executionStatusEnum);
                    return;
                }
            }
        }

//        run tasks for all outgoing nodes
        for (final var outgoingNode : node.getOutgoingNodes()) {
            log.debug("Execution {}: check outgoing nodes for {} node ... ", execution.getId(), node.getId());
//        node can be executed if all previous nodes are executed successfully
            boolean canBeExecuted = true;
            for (final var incomingNode : outgoingNode.getIncomingNodes()) {
                log.debug("Execution {}: Incoming {} node for outgoing {} node has {} status",
                        execution.getId(), incomingNode.getId(), outgoingNode.getId(), nodesIdToStatus.get(incomingNode.getId()));
                if (!nodesIdToStatus.get(incomingNode.getId()).equals(ExecutionStatusEnum.SUCCEEDED)) {
                    canBeExecuted = false;
                    break;
                }
            }

            if (canBeExecuted) {
                log.debug("Execution {}: start executing {} node", execution.getId(), node.getId());
                self.executeNode(execution, outgoingNode);
            }
        }
    }
}
