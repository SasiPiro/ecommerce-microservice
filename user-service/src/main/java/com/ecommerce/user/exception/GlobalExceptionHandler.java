package com.ecommerce.user.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ecommerce.user.constant.ErrorConstants.*;
import static com.ecommerce.user.constant.LogCode.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    // --- 1. CUSTOM EXCEPTION HANDLING ---
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        log.warn("[{}] {}: {}", USER_NOT_FOUND, USER_NOT_FOUND.description(), ex.getMessage());
        return createProblemDetail(ex, HttpStatus.NOT_FOUND, "Resource not found", TYPE_USER_NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        // Determine whether it's a username or email conflict from the exception
        // message
        var logCode = ex.getMessage().toLowerCase().contains("username")
                ? USERNAME_ALREADY_EXISTS
                : EMAIL_ALREADY_EXISTS;
        log.warn("[{}] {}: {}", logCode, logCode.description(), ex.getMessage());
        return createProblemDetail(ex, HttpStatus.CONFLICT, "Data conflict", TYPE_USER_CONFLICT);
    }

    // --- 2. OVERRIDE STANDARD METHOD (DTO VALIDATION) ---
    // Override parent class instead of @ExceptionHandler
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Generic Error",
                        (existing, replacement) -> existing));

        log.warn("[{}] {}: {}", VALIDATION_FAILED, VALIDATION_FAILED.description(), errors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Invalid input data");
        problemDetail.setType(TYPE_VALIDATION_ERROR);
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("service", serviceName);
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    // --- 3. CATCH-ALL ---
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtException(Exception ex) {
        log.error("[{}] {}", INTERNAL_ERROR, INTERNAL_ERROR.description(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Contact support if the problem persists");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(TYPE_GENERIC_ERROR);
        problemDetail.setProperty("service", serviceName);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // HELPER METHOD
    private ProblemDetail createProblemDetail(Exception ex, HttpStatus status, String title, URI type) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle(title);
        problemDetail.setType(type);
        problemDetail.setProperty("service", serviceName);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
