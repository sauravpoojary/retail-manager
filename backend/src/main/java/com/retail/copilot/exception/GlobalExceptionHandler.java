package com.retail.copilot.exception;

import com.retail.copilot.dto.ApiError;
import com.retail.copilot.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleStoreNotFound(StoreNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ApiError.builder()
                        .code("STORE_NOT_FOUND")
                        .message(ex.getMessage())
                        .fallback(false)
                        .build()));
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionNotFound(SessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ApiError.builder()
                        .code("SESSION_NOT_FOUND")
                        .message(ex.getMessage())
                        .fallback(false)
                        .build()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        String code = ex.getMessage().contains("whitespace") ? "INVALID_QUERY" : "VALIDATION_ERROR";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ApiError.builder()
                        .code(code)
                        .message(ex.getMessage())
                        .fallback(false)
                        .build()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ApiError.builder()
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .fallback(false)
                        .build()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        // Suppress noisy 404s for browser-generated requests like favicon.ico
        if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            return ResponseEntity.notFound().build();
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ApiError.builder()
                        .code("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .fallback(false)
                        .build()));
    }
}
