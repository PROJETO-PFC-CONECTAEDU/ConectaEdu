package com.conectaedu.api.shared.exceptions;

public class InvalidSchoolStatusException extends RuntimeException {
    public InvalidSchoolStatusException(String message) {
        super(message);
    }
}
