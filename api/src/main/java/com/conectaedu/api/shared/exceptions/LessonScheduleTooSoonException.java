package com.conectaedu.api.shared.exceptions;

public class LessonScheduleTooSoonException extends RuntimeException {
    public LessonScheduleTooSoonException(String message) {
        super(message);
    }
}