package com.corecompass.report.service;

import com.corecompass.report.dto.*;
import com.corecompass.report.entity.WeeklyReportEntity;
import com.corecompass.report.repository.WeeklyReportRepository;
import com.corecompass.report.scheduler.ReportScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.corecompass.report.dto.MonthlyReportDTO;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final WeeklyReportRepository reportRepo;
    private final ReportScheduler        scheduler;

    /**
     * Returns a paginated list of weekly report summaries for a user.
     */
    public Page<ReportSummaryDTO> listReports(UUID userId, Pageable pageable) {
        return reportRepo.findByUserIdOrderByWeekStartDesc(userId, pageable)
            .map(this::toSummaryDTO);
    }

    /**
     * Returns a single full report by ID, scoped to the requesting user.
     */
    public WeeklyReportDTO getReport(UUID userId, UUID reportId) {
        WeeklyReportEntity entity = reportRepo.findById(reportId)
            .filter(r -> r.getUserId().equals(userId))
            .orElseThrow(() -> new NoSuchElementException("Report not found: " + reportId));
        return toFullDTO(entity);
    }

    /**
     * Returns the most recent weekly report for a user, or null if none exist.
     */
    public WeeklyReportDTO getLatestReport(UUID userId) {
        return reportRepo.findByUserIdOrderByWeekStartDesc(userId, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .map(this::toFullDTO)
            .orElse(null);
    }

    /**
     * Manually trigger report generation for a specific user.
     * Generates for the last completed week by default.
     */
    public void generateReportForUser(UUID userId, String weekStartOverride) {
        if (weekStartOverride != null && !weekStartOverride.isBlank()) {
            LocalDate weekStart = LocalDate.parse(weekStartOverride, DateTimeFormatter.ISO_LOCAL_DATE);
            if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
                throw new IllegalArgumentException("weekStart must be a Monday (ISO week start)");
            }
            log.info("Manual report generation for userId={} week={}", userId, weekStart);
            scheduler.generateForUserAndWeek(userId, weekStart);
            return;
        }
        scheduler.generateForUser(userId);
    }

    /**
     * Admin: trigger generation for all active users.
     */
    public void generateForAllUsers() {
        log.info("Admin triggered full report generation");
        scheduler.generateForAllUsers();
    }

    /**
     * Returns monthly aggregated summaries derived from existing weekly reports.
     * No new entity — groups WeeklyReportEntity rows by YearMonth.
     */
    public List<MonthlyReportDTO> listMonthlyReports(UUID userId, int page, int size) {
        // Fetch all weekly reports for this user (latest first)
        List<WeeklyReportEntity> allWeeks =
                reportRepo.findByUserIdOrderByWeekStartDesc(userId, Pageable.unpaged())
                        .getContent();

        if (allWeeks.isEmpty()) return List.of();

        // Group by YearMonth of weekStart
        Map<YearMonth, List<WeeklyReportEntity>> grouped = allWeeks.stream()
                .collect(Collectors.groupingBy(w -> YearMonth.from(w.getWeekStart())));

        // Sort months descending, apply manual pagination
        List<MonthlyReportDTO> result = grouped.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<WeeklyReportEntity>>comparingByKey().reversed())
                .map(e -> toMonthlyDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // Manual pagination
        int from = page * size;
        if (from >= result.size()) return List.of();
        int to = Math.min(from + size, result.size());
        return result.subList(from, to);
    }

    // ── Mappers ───────────────────────────────────────────────────

    private WeeklyReportDTO toFullDTO(WeeklyReportEntity e) {
        return WeeklyReportDTO.builder()
            .id(e.getId())
            .weekStart(e.getWeekStart())
            .weekEnd(e.getWeekEnd())
            .activeGoals(e.getActiveGoals())
            .avgGoalProgress(e.getAvgGoalProgress())
            .todosCompleted(e.getTodosCompleted())
            .workoutsCount(e.getWorkoutsCount())
            .caloriesBurned(e.getCaloriesBurned())
            .avgSleepHours(e.getAvgSleepHours())
            .netSavings(e.getNetSavings())
            .habitScore(e.getHabitScore())
            .insights(e.getInsights())
            .createdAt(e.getCreatedAt())
            .build();
    }

    private ReportSummaryDTO toSummaryDTO(WeeklyReportEntity e) {
        return ReportSummaryDTO.builder()
            .id(e.getId())
            .weekStart(e.getWeekStart())
            .weekEnd(e.getWeekEnd())
            .habitScore(e.getHabitScore())
            .avgGoalProgress(e.getAvgGoalProgress())
            .workoutsCount(e.getWorkoutsCount())
            .createdAt(e.getCreatedAt())
            .build();
    }

    private MonthlyReportDTO toMonthlyDTO(YearMonth month, List<WeeklyReportEntity> weeks) {
        int count = weeks.size();

        double avgGoalProgress   = weeks.stream().mapToDouble(WeeklyReportEntity::getAvgGoalProgress).average().orElse(0);
        int    totalWorkouts     = weeks.stream().mapToInt(WeeklyReportEntity::getWorkoutsCount).sum();
        double totalCalories     = weeks.stream().mapToDouble(WeeklyReportEntity::getCaloriesBurned).sum();
        double avgSleep          = weeks.stream().mapToDouble(WeeklyReportEntity::getAvgSleepHours).average().orElse(0);
        double totalNetSavings   = weeks.stream().mapToDouble(WeeklyReportEntity::getNetSavings).sum();
        int    avgHabitScore     = (int) weeks.stream().mapToInt(WeeklyReportEntity::getHabitScore).average().orElse(0);

        // Collect all insights, deduplicate, keep top 5
        List<String> topInsights = weeks.stream()
                .filter(w -> w.getInsights() != null)
                .flatMap(w -> w.getInsights().stream())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        return MonthlyReportDTO.builder()
                .month(month.toString())
                .weeksCovered(count)
                .avgGoalProgress(Math.round(avgGoalProgress * 10.0) / 10.0)
                .totalWorkouts(totalWorkouts)
                .totalCaloriesBurned(Math.round(totalCalories * 10.0) / 10.0)
                .avgSleepHours(Math.round(avgSleep * 10.0) / 10.0)
                .totalNetSavings(Math.round(totalNetSavings * 100.0) / 100.0)
                .avgHabitScore(avgHabitScore)
                .topInsights(topInsights)
                .build();
    }
}
