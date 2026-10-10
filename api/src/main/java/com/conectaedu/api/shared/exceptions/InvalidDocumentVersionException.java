package com.conectaedu.api.shared.exceptions;

public class InvalidDocumentVersionException extends RuntimeException {
    public InvalidDocumentVersionException(String message) {
        super(message);
    }
}