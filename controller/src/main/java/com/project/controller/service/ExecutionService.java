package com.project.controller.service;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.model.ExecutionStatusEnum;
import org.springframework.stereotype.Service;
import com.project.controller.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ExecutionService {

    @Autowired
    private ExecutionRepository executionRepository;

    public Execution newExecution(Graph graph) {
        final var newExecution = new Execution();
        newExecution.setStatus(ExecutionStatusEnum.RUNNING);
        newExecution.setGraph(graph);
        newExecution.setStartTime(LocalDateTime.now());
        newExecution.setEndTime(LocalDateTime.now());
        return executionRepository.save(newExecution);
    }

    public Execution finishExecution(Execution execution, ExecutionStatusEnum executionStatusEnum) {
        Execution newExecution = executionRepository.findById(execution.getId()).orElseThrow();
        newExecution.setEndTime(LocalDateTime.now());
        newExecution.setStatus(executionStatusEnum);
        return executionRepository.save(newExecution);
    }
}
