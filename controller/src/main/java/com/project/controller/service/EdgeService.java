package com.project.controller.service;

import com.project.controller.entity.Edge;
import com.project.controller.model.EdgeInput;
import com.project.controller.repository.EdgeRepository;
import com.project.controller.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EdgeService {

    @Autowired
    private EdgeRepository edgeRepository;

    @Autowired NodeRepository nodeRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void createEdges(List<EdgeInput> inputEdges, Map<Long, Long> oldIdToId) {
        final var nodes = nodeRepository.findAll();
        final var edges = inputEdges.stream().map(edge -> {
            final var newEdge = new Edge();
            final var translatedEdgeFrom = oldIdToId.get(edge.from());
            final var translatedEdgeTo = oldIdToId.get(edge.to());
            newEdge.setFromNode(nodes.stream().filter(it -> it.getId().equals(translatedEdgeFrom)).findFirst().orElseThrow());
            newEdge.setToNode(nodes.stream().filter(it -> it.getId().equals(translatedEdgeTo)).findFirst().orElseThrow());
            return newEdge;
        }).toList();
        edgeRepository.saveAll(edges);
    }
}
