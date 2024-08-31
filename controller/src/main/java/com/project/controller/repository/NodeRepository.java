package com.project.controller.repository;

import com.project.controller.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NodeRepository extends PagingAndSortingRepository<Node, Long>, JpaRepository<Node, Long> {
}
