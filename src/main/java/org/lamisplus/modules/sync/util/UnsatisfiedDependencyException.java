package org.lamisplus.modules.sync.util;

public class UnsatisfiedDependencyException extends RuntimeException {
    public UnsatisfiedDependencyException(String message) {
        super(message);
    }
}
