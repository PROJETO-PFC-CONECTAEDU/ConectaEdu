package com.conectaedu.api.shared.exceptions;

public class LegalDocumentNotFoundException extends RuntimeException {
    public LegalDocumentNotFoundException(String message) {
        super(message);
    }
}