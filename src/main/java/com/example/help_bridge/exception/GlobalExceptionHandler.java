package com.example.help_bridge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Input data validation error"
        );
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://api.helpbridge.com/errors/validation-error"));

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
        pd.setProperty("errors", errors);
        return pd;
    }

    // Ловить як синтаксично некоректний JSON, так і невідомі (unrecognized) поля,
    // якщо fail-on-unknown-properties увімкнено (Jackson за замовчуванням = true)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadableException(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Malformed JSON request or unknown field present"
        );
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://api.helpbridge.com/errors/validation-error"));
        return pd;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFoundException(NoSuchElementException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("https://api.helpbridge.com/errors/not-found"));
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        pd.setTitle("Conflict State");
        pd.setType(URI.create("https://api.helpbridge.com/errors/conflict"));
        return pd;
    }
}