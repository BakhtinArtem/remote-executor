package com.project.controller.model;

import java.util.List;

public record GraphInput(String name, List<NodeInput> nodes, List<EdgeInput> edges) {

    public List<NodeInput> getSuccessor(NodeInput nodeInput, boolean isDirected) {
        return nodes.stream().filter(nodeInputDescendant -> edges.stream().anyMatch(edgeInput ->
                edgeInput.from().equals(nodeInput.id()) && edgeInput.to().equals(nodeInputDescendant.id()) ||
                        (!isDirected && edgeInput.from().equals(nodeInputDescendant.id()) && edgeInput.to().equals(nodeInput.id())) )).toList();
    }
}
