package com.project.controller.repository;

import com.project.controller.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EdgeRepository extends JpaRepository<Edge, Long>, PagingAndSortingRepository<Edge, Long> {
}
