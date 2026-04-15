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
}
