package com.project.controller.repository;

import com.project.controller.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExecutionRepository extends JpaRepository<Execution, Long>, PagingAndSortingRepository<Execution, Long> {
}
