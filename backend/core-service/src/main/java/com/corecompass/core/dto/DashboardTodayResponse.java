package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight snapshot for mobile home screen.
 * GET /api/v1/dashboard/today
 *
 * Intentionally excludes heavy Feign calls (finance monthly summary,
 * fitness weekly summary, 90-day heatmap) — those stay in GET /dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTodayResponse {

    // ── Goals ────────────────────────────────────────
    private int            activeGoals;
    private BigDecimal     avgGoalProgress;   // avg across top goals
    private List<GoalResponse> topGoals;      // top 3 by progress

    // ── Todos ────────────────────────────────────────
    private int                totalTodosToday;
    private int                completedTodosToday;
    private int                pendingTodosToday;
    private List<TodoResponse> pendingTodos;  // pending only, for task list UI

    // ── Habits ───────────────────────────────────────
    private int habitScore;                   // 0-100, single Feign call
}