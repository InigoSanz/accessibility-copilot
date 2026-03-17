package com.inigosanz.backend.infrastructure.adapter.in.web.exception;

import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ValidationErrorResponse;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import com.inigosanz.backend.shared.exception.UnsafeScanTargetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ValidationErrorResponse response = new ValidationErrorResponse("Validation error", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ScanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScanNotFound(ScanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnsafeScanTargetException.class)
    public ResponseEntity<ErrorResponse> handleUnsafeScanTarget(UnsafeScanTargetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
