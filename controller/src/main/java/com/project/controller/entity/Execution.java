package com.project.controller.entity;

import com.project.controller.model.ExecutionStatusEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "execution")
@lombok.Getter
@lombok.Setter
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_id_seq")
    @SequenceGenerator(name="execution_id_seq", sequenceName = "execution_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private ExecutionStatusEnum status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "graph_id", referencedColumnName = "id")
    private Graph graph;

}
