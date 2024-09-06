package com.project.controller.exception;

public class MultiComponentDetectedException extends Exception {
    public MultiComponentDetectedException() {
        super("Multi-component graph given");
    }
}
