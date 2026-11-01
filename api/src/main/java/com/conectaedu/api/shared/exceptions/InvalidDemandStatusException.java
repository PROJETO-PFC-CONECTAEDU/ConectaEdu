package com.conectaedu.api.shared.exceptions;

public class InvalidDemandStatusException extends RuntimeException {
    public InvalidDemandStatusException(String message) {
        super(message);
    }
}
