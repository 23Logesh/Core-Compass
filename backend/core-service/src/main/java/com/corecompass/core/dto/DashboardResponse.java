package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private int activeGoals;
    private int completedTodosToday;
    private int totalTodosToday;
    private BigDecimal avgGoalProgress;
    private List<GoalResponse> topGoals;        // top 3 by progress
    private List<TodoResponse> todaysTodos;
    private FitnessSummaryDTO fitnessSummary;
    private FinanceSummaryDTO financeSummary;
    private int habitScore;       // 0-100
    private List<HeatmapEntry> heatmap;          // 90-day activity
}
