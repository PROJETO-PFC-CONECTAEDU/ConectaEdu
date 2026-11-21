package com.conectaedu.api.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class BlockedIpException extends RuntimeException {
    public BlockedIpException(String message) {
        super(message);
    }
}
