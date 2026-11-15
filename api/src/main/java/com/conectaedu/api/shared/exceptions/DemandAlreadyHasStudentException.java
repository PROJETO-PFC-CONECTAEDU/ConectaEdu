package com.conectaedu.api.shared.exceptions;

public class DemandAlreadyHasStudentException extends RuntimeException {
    public DemandAlreadyHasStudentException(String message) {
        super(message);
    }
}
