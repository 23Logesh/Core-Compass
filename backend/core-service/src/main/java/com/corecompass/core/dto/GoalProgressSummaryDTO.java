package com.corecompass.core.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GoalProgressSummaryDTO {
    private int    activeGoals;
    private int    completedGoals;
    private double avgProgressPct;
}
