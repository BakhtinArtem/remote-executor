package com.project.controller.repository;

import com.project.controller.entity.Edge;
import com.project.controller.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface EdgeRepository extends JpaRepository<Edge, Long>, PagingAndSortingRepository<Edge, Long> {
    Optional<Edge> findByFromNode(Node fromNode);
}
