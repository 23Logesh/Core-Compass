package com.corecompass.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class PasswordResetDtos {

    @Data
    public static class ForgotPasswordRequest {
        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        private String email;
    }

    @Data
    public static class VerifyOtpRequest {
        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
        private String otp;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Reset token is required")
        private String resetToken;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
        private String newPassword;
    }

    @Data
    public static class VerifyOtpResponse {
        private final String resetToken;   // short-lived — use immediately to reset password
        private final String message;
    }
}