package com.corecompass.report.scheduler;

import com.corecompass.report.client.*;
import com.corecompass.report.entity.WeeklyReportEntity;
import com.corecompass.report.repository.WeeklyReportRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.*;

/**
 * Weekly report generator — fires every Monday at 08:00 IST.
 * Per LLD Section 5.4: fetches data from all services via Feign (parallelStream per user).
 * Each user's report is independent (@Transactional per user).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final CoreClient          coreClient;
    private final FitnessClient       fitnessClient;
    private final FinanceClient       financeClient;
    private final HabitsClient        habitsClient;
    private final WeeklyReportRepository reportRepo;
    private final NotificationClient     notificationClient;

    // Every Monday 08:00 IST
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Kolkata")
    public void generateWeeklyReports() {
        log.info("Weekly report generation started — {}", LocalDate.now());
        List<UUID> userIds = coreClient.getActiveUserIds();
        log.info("Generating reports for {} active users", userIds.size());

        // parallelStream — each user processed independently
        userIds.parallelStream().forEach(userId -> {
            try {
                generateForUser(userId);
            } catch (Exception ex) {
                log.error("Report generation failed for userId={}: {}", userId, ex.getMessage());
            }
        });
        log.info("Weekly report generation complete");
    }

    // Admin manual trigger endpoint support
    public void generateForAllUsers() {
        generateWeeklyReports();
    }

    @Async
    @Transactional
    public void generateForUser(UUID userId) {
        LocalDate weekStart = LocalDate.now()
                .with(WeekFields.ISO.dayOfWeek(), 1).minusWeeks(1);
        generateForUserAndWeek(userId, weekStart);
    }

    /**
     * Rule-based insights engine — detects anomalies and highlights wins.
     * Phase 2 extension: replace with Claude/OpenAI API call for AI-generated tips.
     */
    private List<String> buildInsights(GoalProgressDTO goals,
                                        FitnessSummaryDTO fitness,
                                        FinanceSummaryDTO finance,
                                        int habitScore) {
        List<String> insights = new ArrayList<>();

        // Goal insights
        if (goals.activeGoals() > 0 && goals.avgProgressPct() >= 80) {
            insights.add("🎯 Outstanding! Your goals are " + (int)goals.avgProgressPct() + "% complete on average — you're crushing it!");
        } else if (goals.avgProgressPct() < 20) {
            insights.add("🎯 Your goal progress is at " + (int)goals.avgProgressPct() + "%. Try breaking your goals into smaller daily todos.");
        }

        // Fitness insights
        if (fitness.workoutsThisWeek() == 0) {
            insights.add("💪 No workouts logged this week. Even a 20-minute walk counts — start small!");
        } else if (fitness.workoutsThisWeek() >= 5) {
            insights.add("💪 Excellent consistency — " + fitness.workoutsThisWeek() + " workouts this week! Your streak is building.");
        }

        // Sleep insights
        if (fitness.avgSleepHours() > 0 && fitness.avgSleepHours() < 6.5) {
            insights.add("😴 Averaging " + String.format("%.1f", fitness.avgSleepHours()) + "h sleep — below optimal. Aim for 7-8 hours for peak performance.");
        } else if (fitness.avgSleepHours() >= 7.5) {
            insights.add("😴 Great sleep this week! Averaging " + String.format("%.1f", fitness.avgSleepHours()) + " hours.");
        }

        // Finance insights
        if (finance.netSavings() < 0) {
            insights.add("💰 You spent more than you earned this month. Review your budget categories to find savings opportunities.");
        } else if (finance.netSavings() > 0) {
            double rate = finance.monthlyIncome() > 0
                ? (finance.netSavings() / finance.monthlyIncome()) * 100 : 0;
            insights.add(String.format("💰 Saved ₹%.0f this month (%.0f%% savings rate). Keep it up!", finance.netSavings(), rate));
        }

        // Habit insights
        if (habitScore >= 80) {
            insights.add("✅ Habit score: " + habitScore + "/100 — incredible discipline! Consistency is compounding.");
        } else if (habitScore < 30) {
            insights.add("✅ Habit score: " + habitScore + "/100. Focus on just 2-3 keystone habits this week.");
        }

        if (insights.isEmpty()) {
            insights.add("📊 Keep logging your data consistently for personalised insights next week!");
        }

        return insights;
    }

    // Add this public method at the bottom of ReportScheduler (before buildInsights)
    public void generateForUserAndWeek(UUID userId, LocalDate weekStart) {
        LocalDate weekEnd   = weekStart.plusDays(6);
        String weekStartStr = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String month        = YearMonth.from(weekStart).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        if (reportRepo.existsByUserIdAndWeekStart(userId, weekStart)) {
            log.debug("Report already exists for userId={} week={}", userId, weekStart);
            return;
        }

        try {
            GoalProgressDTO   goals   = coreClient.getGoalProgress(userId);
            FitnessSummaryDTO fitness = fitnessClient.getWeeklySummary(userId, weekStartStr);
            FinanceSummaryDTO finance = financeClient.getMonthlySummary(userId, month);
            int habitScore            = habitsClient.getHabitScore(userId);
            List<String> insights     = buildInsights(goals, fitness, finance, habitScore);

            WeeklyReportEntity report = WeeklyReportEntity.builder()
                    .userId(userId)
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .activeGoals(goals.activeGoals())
                    .avgGoalProgress(goals.avgProgressPct())
                    .workoutsCount(fitness.workoutsThisWeek())
                    .caloriesBurned(fitness.caloriesBurnedThisWeek())
                    .avgSleepHours(fitness.avgSleepHours())
                    .netSavings(finance.netSavings())
                    .habitScore(habitScore)
                    .insights(insights)
                    .build();

            reportRepo.save(report);
            log.info("Manual report saved: userId={} week={}", userId, weekStart);

            // Push in-app notification — best effort (fallback handles failure)
            String topInsight = insights.isEmpty()
                    ? "Your weekly summary is ready. Check it out!"
                    : insights.getFirst();
            notificationClient.createNotification(
                    userId,
                    "WEEKLY_REPORT",
                    "📊 Your Weekly Report is Ready",
                    topInsight
            );

        } catch (Exception ex) {
            log.error("Failed to generate report for userId={} week={}: {} from generateForUserAndWeek", userId, weekStart, ex.getMessage());
        }
    }
}
