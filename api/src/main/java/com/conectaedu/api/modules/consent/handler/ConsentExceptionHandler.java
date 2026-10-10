package com.conectaedu.api.modules.consent.handler;

import com.conectaedu.api.shared.exceptions.ConsentAlreadyGivenException;
import com.conectaedu.api.shared.exceptions.ConsentNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidDocumentVersionException;
import com.conectaedu.api.shared.exceptions.InvalidLegalDocumentStatusException;
import com.conectaedu.api.shared.exceptions.LegalDocumentNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

//Traduz as exceções do módulo consent em status HTTP.
//Restrito ao pacote do consent: não altera o comportamento dos outros módulos.

@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.consent")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConsentExceptionHandler {

    //404 - recurso inexistente
    @ExceptionHandler({
            LegalDocumentNotFoundException.class,
            ConsentNotFoundException.class,
            UserNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //409 - o recurso existe, mas o estado atual não permite a operação
    @ExceptionHandler({
            InvalidDocumentVersionException.class,
            InvalidLegalDocumentStatusException.class,
            ConsentAlreadyGivenException.class
    })
    public ProblemDetail conflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    //400 - publicador sem permissão
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    //400 - campos reprovados pelo Bean Validation
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