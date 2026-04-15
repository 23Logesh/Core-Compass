package com.corecompass.report.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.time.*;
import java.util.*;

@Entity @Table(name="weekly_reports",schema="report_schema",
    indexes=@Index(name="idx_reports_user_week",columnList="user_id,week_start"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WeeklyReportEntity {
    @Id @UuidGenerator @Column(updatable=false,nullable=false) private UUID id;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(name="week_start",nullable=false) private LocalDate weekStart;
    @Column(name="week_end",nullable=false) private LocalDate weekEnd;
    // Summary stats
    @Column(name="active_goals") private int activeGoals;
    @Column(name="avg_goal_progress") private double avgGoalProgress;
    @Column(name="todos_completed") private int todosCompleted;
    @Column(name="workouts_count") private int workoutsCount;
    @Column(name="calories_burned") private double caloriesBurned;
    @Column(name="avg_sleep_hours") private double avgSleepHours;
    @Column(name="net_savings") private double netSavings;
    @Column(name="habit_score") private int habitScore;
    // AI-style insights stored as JSON array of strings
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb")
    private List<String> insights;
    @Column(name="created_at",updatable=false) @Builder.Default private Instant createdAt=Instant.now();
}
