package com.project.controller.entity;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "edge")
@lombok.Getter
@lombok.Setter
public class Edge implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "edge_id_seq")
    @SequenceGenerator(name="edge_id_seq", sequenceName = "edge_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_node_id", referencedColumnName = "id")
    private Node fromNode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_node_id", referencedColumnName = "id")
    private Node toNode;
}
