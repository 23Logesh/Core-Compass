package com.corecompass.fitness.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class HydrationUpdateRequest {
    @Min(1) private Integer amountMl;
    private Integer targetMl;
}