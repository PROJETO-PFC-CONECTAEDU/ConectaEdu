package com.conectaedu.api.shared.exceptions;

public class LessonOverlapException extends RuntimeException {
    public LessonOverlapException(String message) {
        super(message);
    }
}

