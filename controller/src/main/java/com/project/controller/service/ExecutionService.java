package com.project.controller.service;

import org.springframework.stereotype.Service;
import com.project.controller.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExecutionService {

    @Autowired
    private ExecutionRepository executionRepository;

}
