package com.project.controller.repository;


import com.project.controller.entity.Graph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface GraphRepository extends PagingAndSortingRepository<Graph, Long>, JpaRepository<Graph, Long> {

    @EntityGraph(value = "graph-with-nodes", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Graph> findById(Long id);

    Optional<Long> deleteGraphById(Long id);
}
