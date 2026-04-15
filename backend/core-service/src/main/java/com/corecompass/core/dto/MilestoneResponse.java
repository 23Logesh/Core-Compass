package com.corecompass.core.dto;

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
public class MilestoneResponse {
    private UUID id;
    private String title;
    private LocalDate targetDate;
    private boolean completed;
    private Instant completedAt;
}
