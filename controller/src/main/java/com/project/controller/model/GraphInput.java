package com.project.controller.model;

import java.util.List;

public record GraphInput(String name, List<NodeInput> nodes, List<EdgeInput> edges) {
}
