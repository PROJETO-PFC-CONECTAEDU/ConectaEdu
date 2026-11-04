package com.conectaedu.api.modules.report.handler;

import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.report")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReportExceptionHandler {

    //404 estudante ou escola inexistente.
    @ExceptionHandler({
            UserNotFoundException.class,
            SchoolNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //403
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    //400 data de início posterior à data de término.
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail illegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
