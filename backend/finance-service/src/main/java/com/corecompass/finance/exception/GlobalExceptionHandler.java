package com.corecompass.finance.exception;
import com.corecompass.finance.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
        boolean notFound = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found");
        boolean conflict = ex.getMessage() != null && (ex.getMessage().contains("Already") || ex.getMessage().contains("Duplicate"));
        HttpStatus status = notFound ? HttpStatus.NOT_FOUND : conflict ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        String code = notFound ? "NOT_FOUND" : conflict ? "CONFLICT" : "BAD_REQUEST";
        return ResponseEntity.status(status).body(ApiResponse.error(code, ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fe = ex.getBindingResult().getFieldError();
        String field = fe != null ? fe.getField() : "unknown";
        String msg   = fe != null ? fe.getDefaultMessage() : "Validation failed";
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.fieldError("VALIDATION_FAILED", msg, field));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception in finance", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("INTERNAL_ERROR", "Unexpected error"));
    }
}
