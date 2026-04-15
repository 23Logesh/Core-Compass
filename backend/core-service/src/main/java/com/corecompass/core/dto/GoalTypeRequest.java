package com.corecompass.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GoalTypeRequest {
    @NotBlank
    @Size(min = 2, max = 60)
    private String name;
    private String icon;
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;
    private String unit;
    private boolean isPublic;
}
