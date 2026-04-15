package com.corecompass.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// API ENVELOPE
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

    public static <T> ApiResponse<T> ok(T d, String m) {
        return ApiResponse.<T>builder().success(true).data(d).message(m).build();
    }

    public static <T> ApiResponse<T> error(String c, String m) {
        return ApiResponse.<T>builder().success(false).error(new ErrorDetail(c, m, null)).build();
    }

    public static <T> ApiResponse<T> fieldError(String c, String m, String f) {
        return ApiResponse.<T>builder().success(false).error(new ErrorDetail(c, m, f)).build();
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
