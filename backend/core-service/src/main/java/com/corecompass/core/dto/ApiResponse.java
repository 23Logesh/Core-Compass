package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorDetail error;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder().success(true).data(data).message(message).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder().success(false)
                .error(new ErrorDetail(code, message, null)).build();
    }

    public static <T> ApiResponse<T> fieldError(String code, String msg, String field) {
        return ApiResponse.<T>builder().success(false)
                .error(new ErrorDetail(code, msg, field)).build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private String field;
    }
}
