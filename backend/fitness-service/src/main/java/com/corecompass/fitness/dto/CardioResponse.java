package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardioResponse {
    private UUID id; private String cardioType; private Integer durationMinutes;
    private BigDecimal distanceKm; private Integer caloriesBurned;
    private LocalDate loggedDate; private Instant createdAt;
}
