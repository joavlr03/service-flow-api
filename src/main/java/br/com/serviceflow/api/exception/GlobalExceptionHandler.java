package br.com.serviceflow.api.exception;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                "Existem campos inválidos", fields);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> credentials(BadCredentialsException exception) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ApiError> notFound(EntityNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> business(IllegalArgumentException exception) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(SetupAlreadyCompletedException.class)
    ResponseEntity<ApiError> setupCompleted(SetupAlreadyCompletedException exception) {
        return response(HttpStatus.CONFLICT, "SETUP_ALREADY_COMPLETED", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), code, message, fields, Instant.now()));
    }
}
