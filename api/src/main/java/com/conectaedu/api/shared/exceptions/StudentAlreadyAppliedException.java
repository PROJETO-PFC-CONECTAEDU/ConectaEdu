package com.conectaedu.api.shared.exceptions;

public class StudentAlreadyAppliedException extends RuntimeException {
    public StudentAlreadyAppliedException(String message) {
        super(message);
    }
}
