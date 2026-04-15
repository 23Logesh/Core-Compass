package com.corecompass.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class GoalRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 120, message = "Title must be 3–120 characters")
    private String title;

    @NotNull(message = "categoryTypeId is required")
    private UUID categoryTypeId;

    @NotNull(message = "targetDate is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code like #2D6A9F")
    private String color;

    private String icon;

    @Valid
    private List<MilestoneRequest> milestones;
}
