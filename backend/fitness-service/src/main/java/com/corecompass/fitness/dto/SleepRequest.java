package com.corecompass.fitness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SleepRequest {
    private LocalDate sleepDate;
    private LocalTime bedtime;
    private LocalTime wakeTime;
    @Min(1)
    @Max(5)
    private Integer qualityRating;
    private String notes;
}
