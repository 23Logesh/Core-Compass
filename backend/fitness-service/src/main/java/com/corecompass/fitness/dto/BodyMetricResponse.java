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
public class BodyMetricResponse {
    private UUID id; private String metricType; private BigDecimal value;
    private String unit; private LocalDate loggedDate; private Instant createdAt;
}
