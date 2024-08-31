package com.project.controller.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedEntityGraphs({
        @NamedEntityGraph(name = "graph-with-nodes",
                attributeNodes = {  @NamedAttributeNode(value = "nodes", subgraph = "nodes-with-edges") }, subgraphs = {
            @NamedSubgraph(name = "nodes-with-edges", attributeNodes = { @NamedAttributeNode(value = "edges") })
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // not very efficient
    @JoinColumn(name = "graph_id", referencedColumnName = "id")
    private List<Node> nodes = new ArrayList<>();
}
