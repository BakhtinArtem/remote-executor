package com.project.controller.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "node")
@lombok.Getter
@lombok.Setter
public class Node implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "node_id_seq")
    @SequenceGenerator(name="node_id_seq", sequenceName = "node_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String image;

    @Column(name = "graph_id", nullable = false)
    private Long graphId;

    @Column(name = "is_root",nullable = false)
    private Boolean isRoot;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_node_id", referencedColumnName = "id")
    private List<Edge> edges;
}
