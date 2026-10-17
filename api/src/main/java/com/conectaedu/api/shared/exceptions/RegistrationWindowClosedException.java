package com.conectaedu.api.shared.exceptions;

public class RegistrationWindowClosedException extends RuntimeException {
    public RegistrationWindowClosedException(String message) {
        super(message);
    }
}
