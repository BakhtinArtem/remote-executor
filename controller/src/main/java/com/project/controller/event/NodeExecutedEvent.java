package com.project.controller.event;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Node;
import com.project.controller.model.ExecutionStatusEnum;

public record NodeExecutedEvent(Execution execution, Node node, ExecutionStatusEnum executionStatusEnum) {
}
