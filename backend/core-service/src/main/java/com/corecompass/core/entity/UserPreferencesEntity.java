package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_preferences", schema = "core_schema")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPreferencesEntity {

    // user_id IS the PK — one row per user
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    // LIGHT | DARK | SYSTEM
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String theme = "SYSTEM";

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    // METRIC | IMPERIAL
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String units = "METRIC";

    @Column(name = "weekly_report", nullable = false)
    @Builder.Default
    private boolean weeklyReport = true;

    @Column(name = "budget_alerts", nullable = false)
    @Builder.Default
    private boolean budgetAlerts = true;

    @Column(name = "habit_reminders", nullable = false)
    @Builder.Default
    private boolean habitReminders = true;

    // User-configurable dashboard widget layout stored as JSON
    // null = use default layout on frontend
    @Column(name = "widget_layout", columnDefinition = "jsonb")
    private String widgetLayout;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}