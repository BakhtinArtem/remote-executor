package com.project.controller.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
//  graph with nodes, which include outgoing nodes and incoming nodes
@NamedEntityGraphs({
        @NamedEntityGraph(name = "graph-with-nodes",
                attributeNodes = {  @NamedAttributeNode(value = "nodes", subgraph = "nodes-with-edges") }, subgraphs = {
            @NamedSubgraph(name = "nodes-with-edges", attributeNodes = {
                    @NamedAttributeNode(value = "incomingNodes"),
                    @NamedAttributeNode(value = "outgoingNodes") })
        }),
})
@Table(name = "graph")
@lombok.Getter
@lombok.Setter
public class Graph implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "graph_id_seq")
    @SequenceGenerator(name="graph_id_seq", sequenceName = "graph_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column
    private String name;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "graph")
    private List<Node> nodes = new ArrayList<>();

    @Transient
    private List<Edge> edges;
}
