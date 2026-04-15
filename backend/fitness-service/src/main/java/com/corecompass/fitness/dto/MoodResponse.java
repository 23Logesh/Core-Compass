package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodResponse {
    private UUID id; private String mood; private Integer energyLevel;
    private LocalDate loggedDate; private Instant createdAt;
}
