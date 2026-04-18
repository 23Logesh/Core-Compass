package com.corecompass.habits.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HabitCategoryTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 60, message = "Name must be under 60 characters")
    private String name;

    @Size(max = 10)
    private String icon;   // e.g. "💪"

    @Size(max = 7)
    private String color;  // e.g. "#FF5733"
}