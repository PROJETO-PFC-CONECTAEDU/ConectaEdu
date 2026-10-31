package com.conectaedu.api.modules.feedback.handler;

import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.EvaluationAlreadySubmittedException;
import com.conectaedu.api.shared.exceptions.EvaluationNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidDemandStatusException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.feedback")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeedbackExceptionHandler {

    //404
    @ExceptionHandler({
            EvaluationNotFoundException.class,
            DemandNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //409 a necessidade ainda não terminou, ou a avaliação já foi enviada.
    @ExceptionHandler({
            InvalidDemandStatusException.class,
            EvaluationAlreadySubmittedException.class
    })
    public ProblemDetail conflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail duplicated(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Esta avaliação já foi enviada.");
    }

    //400 impossível avaliar um estudante que não atendeu a necessidade.
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail illegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    //403
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    //400 nota fora de 0 a 10.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Requisição com campos inválidos");
        problem.setProperty("campos", fields);
        return problem;
    }
}
