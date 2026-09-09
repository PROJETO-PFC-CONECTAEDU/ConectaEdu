package com.conectaedu.api.shared.exceptions;

public class InvalidUniversityStatusException extends RuntimeException {
    public InvalidUniversityStatusException(String message) {
        super(message);
    }
}