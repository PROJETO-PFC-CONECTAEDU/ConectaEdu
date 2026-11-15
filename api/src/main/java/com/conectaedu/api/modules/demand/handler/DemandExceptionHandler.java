package com.conectaedu.api.modules.demand.handler;

import com.conectaedu.api.shared.exceptions.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.demand")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DemandExceptionHandler {

    @ExceptionHandler({
            DemandNotFoundException.class,
            UserNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            InvalidDemandStatusException.class,
            DemandAlreadyHasStudentException.class,
            StudentAlreadyAppliedException.class,
            StudentNotLinkedException.class
    })
    public ProblemDetail conflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            NoPendingChangesException.class,
            StudentNotACandidateException.class
    })
    public ProblemDetail badRequest(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
