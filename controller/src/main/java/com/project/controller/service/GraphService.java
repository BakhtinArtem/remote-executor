package com.project.controller.service;

import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.model.GraphInput;
import com.project.controller.repository.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GraphService {

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

    public List<Graph> getGraphs(Pageable pageable) {
        return graphRepository.findAll(pageable).toList();
    }

    public Graph graphById(Long id) {
        return graphRepository.findById(id).orElseThrow();
    }

    public List<Node> getGraphNodes(Long id) {
        return graphById(id).getNodes();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Graph createGraph(GraphInput graphInput) {
        var newGraph = new Graph();
        newGraph.setName(graphInput.name());
        newGraph.setNodes(List.of());
        newGraph = graphRepository.save(newGraph);
        final var map = nodeService.createNodesWithMap(graphInput.nodes(), newGraph.getId());
        edgeService.createEdges(graphInput.edges(), map);
        return newGraph;
    }
}
