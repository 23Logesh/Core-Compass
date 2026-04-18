package com.corecompass.fitness.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardioUpdateRequest {
    private String cardioType;
    @Min(1) private Integer durationMinutes;
    private BigDecimal distanceKm;
    private Integer caloriesBurned;
    private Integer avgHeartRate;
    private LocalDate loggedDate;
    private String notes;
}