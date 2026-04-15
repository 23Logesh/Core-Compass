package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "todos",
    schema = "core_schema",
    indexes = {
        @Index(name = "idx_todos_goal_id",      columnList = "goal_id"),
        @Index(name = "idx_todos_user_due",      columnList = "user_id,due_date"),
        @Index(name = "idx_todos_user_id",       columnList = "user_id")
    }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TodoEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "due_time")
    private LocalTime dueTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    // DAILY | WEEKLY | MONTHLY | null (one-time)
    @Column(name = "recurrence_rule", length = 20)
    private String recurrenceRule;

    // Google Calendar event ID (set after async sync)
    @Column(name = "calendar_event_id", length = 255)
    private String calendarEventId;

    // Tracks sync attempts
    @Column(name = "calendar_sync_attempts")
    @Builder.Default
    private int calendarSyncAttempts = 0;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }
}
