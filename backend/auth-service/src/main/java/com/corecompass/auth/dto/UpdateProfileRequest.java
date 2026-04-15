package com.corecompass.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Size(max = 500)
    private String avatarUrl;
}
