package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class BodyMetricStatsResponse {
    private BigDecimal latestWeightKg;
    private BigDecimal latestHeightCm;
    private BigDecimal bmi;
    private String bmiCategory;
    private BigDecimal tdeeKcal;
    private LocalDate weightDate;
    private LocalDate heightDate;
}