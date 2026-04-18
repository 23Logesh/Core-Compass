package com.corecompass.fitness.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BodyMetricUpdateRequest {
    private BigDecimal value;
    private String unit;
    private LocalDate loggedDate;
    private String notes;
}