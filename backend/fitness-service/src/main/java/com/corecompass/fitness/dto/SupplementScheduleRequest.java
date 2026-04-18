package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SupplementScheduleRequest {

    @NotNull
    private UUID supplementTypeId;

    @NotNull @DecimalMin("0.01")
    private BigDecimal doseAmount;

    @NotBlank
    @Pattern(regexp = "GRAM|CAPSULE|ML|MG|TABLET|SCOOP")
    private String doseUnit;

    @NotBlank
    @Pattern(regexp = "MORNING|AFTERNOON|EVENING|NIGHT|PRE_WORKOUT|POST_WORKOUT")
    private String timing;

    // DAILY or comma-separated days: MON,WED,FRI
    @Size(max = 50)
    private String frequency;
}