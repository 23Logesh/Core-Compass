package com.corecompass.report.exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.*;
@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success",false,"error",Map.of("code","NOT_FOUND","message",ex.getMessage()),"timestamp",Instant.now().toString()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleBadReq(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("success",false,"error",Map.of("code","BAD_REQUEST","message",ex.getMessage()),"timestamp",Instant.now().toString()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneral(Exception ex) {
        log.error("Unhandled in report-service", ex);
        return ResponseEntity.status(500).body(Map.of("success",false,"error",Map.of("code","INTERNAL_ERROR","message","Unexpected error"),"timestamp",Instant.now().toString()));
    }
}
