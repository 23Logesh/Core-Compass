package com.corecompass.core.service;

import com.corecompass.core.client.FitnessClient;
import com.corecompass.core.client.FinanceClient;
import com.corecompass.core.client.HabitsClient;
import com.corecompass.core.dto.*;
import com.corecompass.core.repository.GoalRepository;
import com.corecompass.core.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoalRepository    goalRepository;
    private final TodoRepository    todoRepository;
    private final GoalService       goalService;
    private final FitnessClient     fitnessClient;
    private final FinanceClient     financeClient;
    private final HabitsClient      habitsClient;

    /**
     * Builds unified dashboard.
     *
     * Feign calls to fitness, finance, habits run with circuit-breaker fallbacks,
     * so this always returns a valid response even if downstream services are down.
     */
    public DashboardResponse getDashboard(UUID userId) {
        LocalDate today = LocalDate.now();

        // ── Core stats (local DB) ───────────────────────────
        long activeGoals = goalRepository
            .countByUserIdAndStatusAndIsDeletedFalse(userId, "ACTIVE");

        List<TodoResponse> todaysTodos = todoRepository
            .findTodaysTodos(userId, today)
            .stream()
            .map(t -> TodoResponse.builder()
                .id(t.getId())
                .goalId(t.getGoalId())
                .title(t.getTitle())
                .dueTime(t.getDueTime() != null ? t.getDueTime().toString() : null)
                .completed(t.isCompleted())
                .completedAt(t.getCompletedAt())
                .build())
            .collect(Collectors.toList());

        long completedToday = todaysTodos.stream().filter(TodoResponse::isCompleted).count();

        // Top 3 goals by progress
        List<GoalResponse> topGoals = goalRepository
            .findTopGoalsByProgress(userId, PageRequest.of(0, 3))
            .stream()
            .map(goalService::toGoalResponse)
            .collect(Collectors.toList());

        BigDecimal avgProgress = topGoals.isEmpty() ? BigDecimal.ZERO :
            topGoals.stream()
                .map(GoalResponse::getProgressPct)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(topGoals.size()), 2, java.math.RoundingMode.HALF_UP);

        // ── Feign calls (with fallback — never throw) ───────
        String weekStart = today.with(
                WeekFields.ISO.dayOfWeek(), 1
        ).format(DateTimeFormatter.ISO_LOCAL_DATE);

        String month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        FitnessSummaryDTO fitness;
        FinanceSummaryDTO finance;
        int habitScore;

        try { fitness = fitnessClient.getWeeklySummary(userId, weekStart); }
        catch (Exception e) {
            log.warn("fitness-service Feign failed for userId={}: {}", userId, e.getMessage());
            fitness = FitnessSummaryDTO.builder().build();
        }

        try { finance = financeClient.getMonthlySummary(userId, month); }
        catch (Exception e) {
            log.warn("finance-service Feign failed for userId={}: {}", userId, e.getMessage());
            finance = FinanceSummaryDTO.builder().build();
        }

        try { habitScore = habitsClient.getHabitScore(userId); }
        catch (Exception e) {
            log.warn("habits-service Feign failed for userId={}: {}", userId, e.getMessage());
            habitScore = 0;
        }

        // ── Heatmap ─────────────────────────────────────────
        List<HeatmapEntry> heatmap = goalService.getHeatmap(userId);

        return DashboardResponse.builder()
            .activeGoals((int) activeGoals)
            .completedTodosToday((int) completedToday)
            .totalTodosToday(todaysTodos.size())
            .avgGoalProgress(avgProgress)
            .topGoals(topGoals)
            .todaysTodos(todaysTodos)
            .fitnessSummary(fitness)
            .financeSummary(finance)
            .habitScore(habitScore)
            .heatmap(heatmap)
            .build();
    }
}
