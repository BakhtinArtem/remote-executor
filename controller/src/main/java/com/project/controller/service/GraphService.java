package com.project.controller.service;

import com.project.controller.entity.Edge;
import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.exception.CycleDetectedException;
import com.project.controller.exception.MultiComponentDetectedException;
import com.project.controller.model.GraphInput;
import com.project.controller.repository.GraphRepository;
import com.project.controller.util.GraphUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
public class GraphService {

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

    @Autowired
    private RunnerService runnerService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    @Lazy
    private GraphService thisProxy;

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

    @Transactional
    public List<Edge> getGraphEdges(Long id) {
//        bruh ...
          return thisProxy.getGraphNodes(id).stream().map(node -> edgeService.getEdgesForNode(node)).filter(Objects::nonNull).toList();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)  //  to read uncommitted nodes during edge insertion
    public Graph createGraph(GraphInput graphInput) throws CycleDetectedException, MultiComponentDetectedException {
//        validate graph - graph should have one component and be acyclic
        final var cycle = GraphUtil.findCycle(graphInput);
        if (cycle.isPresent()) {
            throw new CycleDetectedException(cycle.get());
        }

        if (GraphUtil.isMultiComponent(graphInput)) {
            throw new MultiComponentDetectedException();
        }

        var newGraph = new Graph();
        newGraph.setName(graphInput.name());
        newGraph.setNodes(List.of());
        newGraph = graphRepository.save(newGraph);
        final var oldIdToId = nodeService.createNodesWithMap(graphInput.nodes(), newGraph);
        edgeService.createEdges(graphInput.edges(), oldIdToId);
        return newGraph;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Graph updateGraph(GraphInput graphInput) {
        final var graph = graphRepository.findById(graphInput.id()).orElseThrow();
        nodeService.deleteGraphNodes(graph);
        final var oldIdToId = nodeService.createNodesWithMap(graphInput.nodes(), graph);
        edgeService.createEdges(graphInput.edges(), oldIdToId);
        return graph;
    }

    @Transactional
    public Execution executeGraph(Long graphId) {
        final var graph = thisProxy.graphById(graphId);
        final var execution = executionService.newExecution(graph);
        runnerService.executeGraph(graph, execution);
        return execution;
    }

    @Transactional
    @Modifying
    public Long deleteGraph(Long graphId) {
        return graphRepository.deleteGraphById(graphId).orElseThrow();
    }

}
