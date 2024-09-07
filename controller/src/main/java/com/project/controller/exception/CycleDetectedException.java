package com.project.controller.exception;

import lombok.Getter;

import java.util.Set;

@Getter
public class CycleDetectedException extends Exception {

    private Set<Long> idsInCycle;

    public CycleDetectedException(Set<Long> idsInCycle) {
        super("Graph has cycle: " + idsInCycle.stream().reduce("", (a, b) -> a + " " + b, String::concat));
        this.idsInCycle = idsInCycle;
    }

    public void setIdsInCycle(Set<Long> idsInCycle) {
        this.idsInCycle = idsInCycle;
    }
}
