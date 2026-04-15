package com.corecompass.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MilestoneRequest {
    @NotBlank
    @Size(max = 120)
    private String title;
    private LocalDate targetDate;
}
