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
public class HydrationResponse {
    private UUID id; private Integer amountMl; private Integer targetMl;
    private LocalDate loggedDate; private Instant createdAt;
}
