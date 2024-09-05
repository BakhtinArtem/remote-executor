package com.project.controller.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

//    setter manual definition due to lombok incorrect generating of this method for HashSet
    @lombok.Setter(lombok.AccessLevel.NONE)
    @ManyToMany
    @JoinTable(name = "edge", joinColumns = @JoinColumn(name = "from_node_id"),
            inverseJoinColumns = @JoinColumn(name = "to_node_id")
    )
    private Set<Node> outgoingNodes = new HashSet<>();

//    same
    @lombok.Setter(lombok.AccessLevel.NONE)
    @ManyToMany
    @JoinTable(name = "edge", joinColumns = @JoinColumn(name = "to_node_id"),
            inverseJoinColumns = @JoinColumn(name = "from_node_id")
    )
    private Set<Node> incomingNodes = new HashSet<>();

    public void setIncomingNodes(Set<Node> incomingNodes) {
        this.incomingNodes = incomingNodes;
    }

    public void setOutgoingNodes(Set<Node> outgoingNodes) {
        this.outgoingNodes = outgoingNodes;
    }
}
