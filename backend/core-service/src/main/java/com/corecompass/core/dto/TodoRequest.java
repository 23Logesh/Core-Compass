package com.corecompass.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TodoRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be 1–255 characters")
    private String title;

    @Size(max = 500)
    private String description;

    private LocalDate dueDate;

    // HH:mm format validated in service
    private String dueTime;

    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "recurrenceRule must be DAILY, WEEKLY or MONTHLY")
    private String recurrenceRule;
}
