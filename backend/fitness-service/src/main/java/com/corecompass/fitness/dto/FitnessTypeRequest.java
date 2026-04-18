package com.corecompass.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FitnessTypeRequest {

    @NotBlank(message = "name is required")
    @Size(max = 60)
    private String name;

    @Size(max = 10)
    private String icon;

    // for cardio/workout/meal types
    @Size(max = 7, message = "color must be a hex code e.g. #FF5733")
    private String color;

    // for metric types only — default unit e.g. kg, cm, %
    @Size(max = 20)
    private String unit;
}