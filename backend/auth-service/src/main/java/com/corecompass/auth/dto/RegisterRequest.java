package com.corecompass.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;
}
