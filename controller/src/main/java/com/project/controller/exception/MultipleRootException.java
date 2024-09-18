package com.project.controller.exception;

public class MultipleRootException extends Exception {
    public MultipleRootException() { super("Graph contains multiple roots"); }
}
