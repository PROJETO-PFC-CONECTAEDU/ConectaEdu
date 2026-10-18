package com.conectaedu.api.modules.lesson.handler;

import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidLessonOriginException;
import com.conectaedu.api.shared.exceptions.InvalidLessonStatusException;
import com.conectaedu.api.shared.exceptions.InvalidSchoolStatusException;
import com.conectaedu.api.shared.exceptions.InvalidUniversityStatusException;
import com.conectaedu.api.shared.exceptions.LessonNotFoundException;
import com.conectaedu.api.shared.exceptions.LessonOverlapException;
import com.conectaedu.api.shared.exceptions.LessonScheduleTooSoonException;
import com.conectaedu.api.shared.exceptions.RegistrationWindowClosedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

//Traduz as exceções do módulo lesson em status HTTP.


@RestControllerAdvice(basePackages = "com.conectaedu.api.modules.lesson")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LessonExceptionHandler {

    //404 recurso inexistente

    @ExceptionHandler({
            LessonNotFoundException.class,
            DemandNotFoundException.class
    })
    public ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //409 a aula existe, mas o estado atual não permite a operação
    @ExceptionHandler({
            InvalidLessonStatusException.class,
            LessonOverlapException.class,
            RegistrationWindowClosedException.class,
            InvalidUniversityStatusException.class,
            InvalidSchoolStatusException.class
    })
    public ProblemDetail conflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    //400 prazo e origem da aula

    @ExceptionHandler({
            LessonScheduleTooSoonException.class,
            InvalidLessonOriginException.class
    })
    public ProblemDetail badRequest(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    //400 entrada incoerente, como cancelar com motivo OTHER sem justificativa.

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail illegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    //403 papel certo, pessoa errada, o diretor de outra escola, o estudante de outra aula.

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    //400 campos reprovados pelo Bean Validation.

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
