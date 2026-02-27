package com.projectsugarglider.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponseRecord> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

        log.warn("HTTP {} {} -> {} ({})",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(status).body(build(status, ex.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseRecord> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("HTTP {} {} -> {} (validation: {})",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                message
        );

        return ResponseEntity.status(status).body(build(status, message, request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponseRecord> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        String message = pickMostUsefulMessage(ex);

        log.warn("HTTP {} {} -> {} (db: {})",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                message
        );

        log.debug("DataIntegrityViolationException detail", ex);

        return ResponseEntity.status(status).body(build(status, message, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseRecord> handleAny(
            Exception ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // 500은 그래도 기록은 남기되, 스택트레이스는 debug에서만
        log.error("HTTP {} {} -> {} ({})",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                ex.getMessage()
        );
        log.debug("Unhandled exception detail", ex);

        return ResponseEntity.status(status).body(build(status, "Internal Server Error", request));
    }

    private ApiErrorResponseRecord build(HttpStatus status, String message, HttpServletRequest request) {
        String requestId = MDC.get("requestId"); 
        return new ApiErrorResponseRecord(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                requestId
        );
    }

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String defaultMessage = fe.getDefaultMessage();
        Object rejected = fe.getRejectedValue();
        return field + "=" + rejected + " (" + defaultMessage + ")";
    }

    private String pickMostUsefulMessage(DataIntegrityViolationException ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = ex.getMessage();
        }
        if (msg == null) {
            return "Data integrity violation";
        }
        msg = msg.replace("\n", " ").replace("\r", " ");
        if (msg.length() > 400) {
            msg = msg.substring(0, 400) + "...";
        }
        return msg;
    }
}