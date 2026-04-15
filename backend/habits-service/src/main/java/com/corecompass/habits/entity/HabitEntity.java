package com.corecompass.habits.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "habits", schema = "habits_schema",
        indexes = {@Index(name = "idx_habits_user_id", columnList = "user_id"),
                @Index(name = "idx_habits_user_status", columnList = "user_id,status")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(length = 300)
    private String description;
    @Column(name = "category_type_id")
    private UUID categoryTypeId;
    @Column(name = "tracking_type", nullable = false, length = 20)
    private String trackingType;
    @Column(name = "frequency_pattern", nullable = false, length = 30)
    private String frequencyPattern;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "frequency_config", columnDefinition = "jsonb")
    private Map<String, Object> frequencyConfig;
    @Column(name = "target_value")
    private Double targetValue;
    @Column(name = "target_unit", length = 30)
    private String targetUnit;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_steps", columnDefinition = "jsonb")
    private List<String> checklistSteps;
    @Column(name = "cue", length = 200)
    private String cue;
    @Column(name = "reward", length = 200)
    private String reward;
    @Column(name = "reminder_time")
    private LocalTime reminderTime;
    @Column(length = 7)
    private String color;
    @Column(length = 10)
    private String icon;
    @Column(name = "start_date")
    @Builder.Default
    private LocalDate startDate = LocalDate.now();
    @Column(name = "current_streak")
    @Builder.Default
    private int currentStreak = 0;
    @Column(name = "best_streak")
    @Builder.Default
    private int bestStreak = 0;
    @Column(name = "total_checkins")
    @Builder.Default
    private int totalCheckins = 0;
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
