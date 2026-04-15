package com.corecompass.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HydrationRequest {
    @NotNull
    @Min(1) private Integer amountMl; private Integer targetMl;
}
