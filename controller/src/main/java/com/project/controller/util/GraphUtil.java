package com.project.controller.util;

import com.project.controller.model.GraphInput;
import com.project.controller.model.NodeInput;

import java.util.*;

public class GraphUtil {

    public static Optional<Set<Long>> findCycle(GraphInput graphInput) {
        for (final var nodeInput : graphInput.nodes()) {
            final var cycle = dfs(graphInput, nodeInput, new HashSet<>(graphInput.nodes().size()));
            if (cycle.isPresent()) { return cycle; }
        }
        return Optional.empty();
    }

    public static boolean isMultiComponent(GraphInput graphInput) {
//        iterate over each node and check components
        final var graphSize = graphInput.nodes().size();
        for (final var nodeInput : graphInput.nodes()) {
            if (bfs(graphInput, nodeInput).size() != graphSize) {
                return true;
            }
        }
        return false;
    }

    private static Optional<Set<Long>> dfs(GraphInput graphInput, NodeInput nodeInput, Set<Long> idsInPath) {
        idsInPath.add(nodeInput.id());
        for (final var nodeInputDescendant : graphInput.getSuccessor(nodeInput, true)) {
            if (idsInPath.contains(nodeInputDescendant.id())) {
                return Optional.of(idsInPath);
            }
            return dfs(graphInput, nodeInputDescendant, idsInPath);
        }
        return Optional.empty();
    }


    private static List<Long> bfs(GraphInput graphInput, NodeInput nodeInput) {
        Queue<NodeInput> queue = new ArrayDeque<>();
        queue.add(nodeInput);
        final var resultNodes = new ArrayList<Long>();
        while (!queue.isEmpty()) {
            final var currNode = queue.remove();
            for (final var successorNode : graphInput.getSuccessor(currNode, false)) {
                if (!resultNodes.contains(successorNode.id())) {
                    queue.add(successorNode);
                    resultNodes.add(successorNode.id());
                }
            }
        }
        return resultNodes;
    }
}
