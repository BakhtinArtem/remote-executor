package com.project.controller.util;

import com.project.controller.model.EdgeInput;
import com.project.controller.model.GraphInput;
import com.project.controller.model.NodeInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
public class GraphUtilTest {

    @Test
    public void testFindCycle_WithCycle() {
        final var nodes = List.of(new NodeInput(1L, "", "", true),
                new NodeInput(2L, "", "", false),
                new NodeInput(3L, "", "", false),
                new NodeInput(4L, "", "", false));
        final var edges = List.of(new EdgeInput(1L,2L), new EdgeInput(2L, 3L), new EdgeInput(3L, 4L), new EdgeInput(4L, 2L));
        final var graphInput = new GraphInput("", nodes, edges);

        Optional<Set<Long>> result = GraphUtil.findCycle(graphInput);

        assertTrue(result.isPresent());
        assertTrue(result.get().contains(2L));
        assertTrue(result.get().contains(3L));
        assertTrue(result.get().contains(4L));
    }

    @Test
    public void isMultiComponent_WithTwoComponent() {
        final var nodes = List.of(new NodeInput(1L, "", "", true),
                new NodeInput(2L, "", "", false),
                new NodeInput(3L, "", "", false),
                new NodeInput(4L, "", "", false));
        final var edges = List.of(new EdgeInput(1L,2L), new EdgeInput(3L, 4L));
        final var graphInput = new GraphInput("", nodes, edges);
        assertTrue(GraphUtil.isMultiComponent(graphInput));
    }

    @Test
    public void isMultiComponent_WithOneComponent() {
        final var nodes = List.of(new NodeInput(1L, "", "", true),
                new NodeInput(2L, "", "", false),
                new NodeInput(3L, "", "", false),
                new NodeInput(4L, "", "", false));
        final var edges = List.of(new EdgeInput(1L,2L), new EdgeInput(2L, 4L), new EdgeInput(3L, 4L));
        final var graphInput = new GraphInput("", nodes, edges);
        assertTrue(GraphUtil.isMultiComponent(graphInput));
    }
}