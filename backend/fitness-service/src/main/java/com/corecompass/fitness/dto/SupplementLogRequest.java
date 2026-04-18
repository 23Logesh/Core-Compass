package com.corecompass.fitness.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SupplementLogRequest {

    @NotNull(message = "supplementTypeId is required")
    private UUID supplementTypeId;

    @NotNull @DecimalMin("0.01")
    private BigDecimal doseAmount;

    @NotBlank
    @Pattern(regexp = "GRAM|CAPSULE|ML|MG|TABLET|SCOOP")
    private String doseUnit;

    @Pattern(regexp = "MORNING|AFTERNOON|EVENING|NIGHT|PRE_WORKOUT|POST_WORKOUT")
    private String timing;

    private LocalDate loggedDate;

    @Size(max = 200)
    private String notes;
}