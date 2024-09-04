package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.event.NodeExecutedEvent;
import com.project.controller.model.ExecutionStatusEnum;
import com.project.controller.model.GraphInput;
import com.project.controller.repository.ExecutionRepository;
import com.project.controller.repository.GraphRepository;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class GraphService {

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private RunnerService runnerService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Lazy
    private GraphService self;

    private final Logger logger = LoggerFactory.getLogger(GraphService.class);

//    each execution associated with map of graph nodes with statuses
    private final Map<Long, Map<Long, ExecutionStatusEnum>> executionIdToRunningNodesMap = new ConcurrentHashMap<>();

    @Transactional
    public List<Graph> getGraphs(Pageable pageable) {
        return graphRepository.findAll(pageable).toList();
    }

    @Transactional
    public Graph graphById(Long id) {
        return graphRepository.findById(id).orElseThrow();
    }

    @Transactional
    public List<Node> getGraphNodes(Long id) {
        return graphById(id).getNodes();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)  //  to read uncommitted nodes during edge insertion
    public Graph createGraph(GraphInput graphInput) {
//        todo: validate graph
        var newGraph = new Graph();
        newGraph.setName(graphInput.name());
        newGraph.setNodes(List.of());
        newGraph = graphRepository.save(newGraph);
        final var oldIdToId = nodeService.createNodesWithMap(graphInput.nodes(), newGraph.getId());
        edgeService.createEdges(graphInput.edges(), oldIdToId);
        return newGraph;
    }

    @Transactional
    public Execution executeGraph(Long graphId) {
        final var graph = self.graphById(graphId);

        final var execution = executionService.newExecution(graph);

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
        runnerService.runTask(execution, root[0]);
        return execution;
    }

//    @Async(value = "threadPoolTaskExecutor")
    @EventListener()
    public void handleNodeExecutedEvent(NodeExecutedEvent nodeExecutedEvent) {
//        todo: lock graph editing if job is running
        final var nodesIdToStatus = executionIdToRunningNodesMap.get(nodeExecutedEvent.execution().getId());
        nodesIdToStatus.put(nodeExecutedEvent.node().getId(), nodeExecutedEvent.executionStatusEnum());
//        failed node execution fails graph execution
        if (nodeExecutedEvent.executionStatusEnum().equals(ExecutionStatusEnum.FAILED)) {
//            todo
            throw new NotImplementedException("Failed node not handled");
        }
//        finish execution if last node (no outgoing nodes exist) executed and others nodes executed as well
        if (nodeExecutedEvent.node().getOutgoingNodes().isEmpty()) {
            boolean finished = true;
            for (final Map.Entry<Long, ExecutionStatusEnum> entry : nodesIdToStatus.entrySet()) {
                if (!entry.getValue().equals(ExecutionStatusEnum.SUCCEEDED)) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                executionService.finishExecution(nodeExecutedEvent.execution(), nodeExecutedEvent.executionStatusEnum());
                return;
            }
        }

//        run tasks for all outgoing nodes
        for (final var node : nodeExecutedEvent.node().getOutgoingNodes()) {
//        node can be executed if all previous nodes are executed successfully
            boolean canBeExecuted = true;
            for (final var incomingNode : node.getIncomingNodes()) {
                if (!nodesIdToStatus.get(incomingNode.getId()).equals(ExecutionStatusEnum.SUCCEEDED)) {
                    canBeExecuted = false;
                    break;
                }
            }

            if (canBeExecuted) {
                runnerService.runTask(nodeExecutedEvent.execution(), node);
            }
        }
    }
}
