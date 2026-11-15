package com.conectaedu.api.modules.internship.handler;

import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Traduz as exceções do módulo internship em status HTTP.

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.internship")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternshipExceptionHandler {

    //404 o estudante ou a universidade da rota não existe.

    @ExceptionHandler({
            UserNotFoundException.class,
            UniversityNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //403 papel certo, pessoa errada.

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
