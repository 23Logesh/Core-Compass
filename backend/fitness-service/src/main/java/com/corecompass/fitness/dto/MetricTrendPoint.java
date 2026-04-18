package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class MetricTrendPoint {
    private LocalDate date;
    private BigDecimal value;
    private String unit;
}