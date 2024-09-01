package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.model.ExecutionStatusEnum;
import com.project.controller.model.GraphInput;
import com.project.controller.repository.ExecutionRepository;
import com.project.controller.repository.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
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
    @Lazy
    private GraphService self;

//    each execution associated with map of graph nodes with statuses
    private final Map<Long, Map<Long, ExecutionStatusEnum>> executionIdToRunningNodesMap = new ConcurrentHashMap<>();

    public List<Graph> getGraphs(Pageable pageable) {
        return graphRepository.findAll(pageable).toList();
    }

    public Graph graphById(Long id) {
        return graphRepository.findById(id).orElseThrow();
    }

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

    public Execution executeGraph(Long graphId) {
        final var graph = self.graphById(graphId);

        final var execution = new Execution();
        execution.setStatus(ExecutionStatusEnum.RUNNING);
        execution.setGraph(graph);
        execution.setStartTime(LocalDateTime.now());
//        fixme
        execution.setEndTime(LocalDateTime.now());
        executionRepository.save(execution);

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

//        listen for event
        runnerService.runTask(execution, root[0]);

        return execution;
    }
}
