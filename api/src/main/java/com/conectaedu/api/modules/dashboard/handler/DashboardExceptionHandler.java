package com.conectaedu.api.modules.dashboard.handler;

import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.dashboard")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DashboardExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail notFound(UserNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acesso negado às estatísticas.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar estatísticas do dashboard.");
    }
}
