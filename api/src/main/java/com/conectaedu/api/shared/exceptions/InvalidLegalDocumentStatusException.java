package com.conectaedu.api.shared.exceptions;

public class InvalidLegalDocumentStatusException extends RuntimeException {
    public InvalidLegalDocumentStatusException(String message) {
        super(message);
    }
}