package com.conectaedu.api.shared.exceptions;

public class EvaluationAlreadySubmittedException extends RuntimeException {
    public EvaluationAlreadySubmittedException(String message) {
        super(message);
    }
}
