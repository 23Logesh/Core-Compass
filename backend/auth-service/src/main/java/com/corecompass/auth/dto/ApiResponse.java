package com.corecompass.auth.dto;

import lombok.*;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T       data;
    private String  message;
    private ErrorDetail error;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorDetail(code, message, null))
            .build();
    }

    public static <T> ApiResponse<T> fieldError(String code, String message, String field) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorDetail(code, message, field))
            .build();
    }

    @Data @AllArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private String field;
    }
}
