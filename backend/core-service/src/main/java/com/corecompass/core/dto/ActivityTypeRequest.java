package com.corecompass.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivityTypeRequest {
    @NotBlank(message = "name is required")
    @Size(max = 60)
    private String name;

    @Size(max = 10)
    private String icon;

    @Size(max = 7, message = "color must be a hex code e.g. #FF5733")
    private String color;
}