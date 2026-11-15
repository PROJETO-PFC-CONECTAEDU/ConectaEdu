package com.conectaedu.api.shared.exceptions;

public class StudentNotACandidateException extends RuntimeException {
    public StudentNotACandidateException(String message) {
        super(message);
    }
}
