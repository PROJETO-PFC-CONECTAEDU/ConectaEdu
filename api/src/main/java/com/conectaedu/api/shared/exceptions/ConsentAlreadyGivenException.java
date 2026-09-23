package com.conectaedu.api.shared.exceptions;

public class ConsentAlreadyGivenException extends RuntimeException {
    public ConsentAlreadyGivenException(String message) {
        super(message);
    }
}