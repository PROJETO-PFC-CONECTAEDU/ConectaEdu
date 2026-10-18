package com.conectaedu.api.shared.exceptions;

public class DemandNotFoundException extends RuntimeException {
    public DemandNotFoundException(String message) {
        super(message);
    }
}