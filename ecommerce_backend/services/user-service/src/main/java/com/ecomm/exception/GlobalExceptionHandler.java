package com.ecomm.exception;
import com.ecomm.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORR_KEY = "cid";

    private ErrorResponse base(HttpStatus status, String code, String message, String path) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .correlationId(MDC.get(CORR_KEY))
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("NotFound: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(base(HttpStatus.NOT_FOUND, "USR_404", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(base(HttpStatus.CONFLICT, "USR_409", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var body = base(HttpStatus.BAD_REQUEST, "VAL_400", "Validation failed", req.getRequestURI()).toBuilder()
                .fieldErrors(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(f -> ErrorResponse.FieldErrorItem.builder()
                                        .field(f.getField())
                                        .message(f.getDefaultMessage())
                                        .rejectedValue(f.getRejectedValue())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
        log.debug("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var body = base(HttpStatus.BAD_REQUEST, "VAL_400", "Validation failed", req.getRequestURI()).toBuilder()
                .fieldErrors(
                        ex.getConstraintViolations().stream()
                                .map(this::toFieldItem)
                                .collect(Collectors.toList())
                )
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    private ErrorResponse.FieldErrorItem toFieldItem(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
        return ErrorResponse.FieldErrorItem.builder()
                .field(field)
                .message(v.getMessage())
                .rejectedValue(v.getInvalidValue())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(base(HttpStatus.FORBIDDEN, "SEC_403", "Access is denied", req.getRequestURI()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(base(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_415", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base(HttpStatus.BAD_REQUEST, "REQ_400", "Malformed JSON request", req.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(org.springframework.security.core.AuthenticationException ex, HttpServletRequest req) {
        log.warn("Unauthenticated: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(base(HttpStatus.UNAUTHORIZED, "SEC_401", "Authentication required", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        // last-resort catch – keep message generic
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(base(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_500", "Internal server error", req.getRequestURI()));
    }
}
