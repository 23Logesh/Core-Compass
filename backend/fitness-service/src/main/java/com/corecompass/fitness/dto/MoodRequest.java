package com.corecompass.fitness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MoodRequest {
    @NotBlank
    @Pattern(regexp = "GREAT|GOOD|NEUTRAL|TIRED|SICK") private String mood;
    @Min(1) @Max(10) private Integer energyLevel; private String notes;
}
