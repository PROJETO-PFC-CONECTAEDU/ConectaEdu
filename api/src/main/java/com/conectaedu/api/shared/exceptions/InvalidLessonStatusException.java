package com.conectaedu.api.shared.exceptions;

public class InvalidLessonStatusException extends RuntimeException {
    public InvalidLessonStatusException(String message) {
        super(message);
    }
}