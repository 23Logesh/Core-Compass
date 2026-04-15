package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private UUID id;
    private String title;
    private GoalTypeDTO categoryType;
    private LocalDate targetDate;
    private String description;
    private BigDecimal progressPct;
    private String status;
    private String color;
    private String icon;
    private int todoCount;
    private int completedTodos;
    private List<MilestoneResponse> milestones;
    private Instant createdAt;
    private Instant updatedAt;
}
