package com.conectaedu.api.shared.exceptions;

public class NoPendingChangesException extends RuntimeException {
    public NoPendingChangesException(String message) {
        super(message);
    }
}
