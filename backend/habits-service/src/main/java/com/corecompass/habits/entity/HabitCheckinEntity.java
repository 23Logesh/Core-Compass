package com.corecompass.habits.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "habit_checkins", schema = "habits_schema",
        indexes = {@Index(name = "idx_checkins_habit_date", columnList = "habit_id,checkin_date"),
                @Index(name = "idx_checkins_user_date", columnList = "user_id,checkin_date")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitCheckinEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;
    @Column(name = "habit_id", nullable = false)
    private UUID habitId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;
    @Column
    private Double value;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_completed", columnDefinition = "jsonb")
    private List<Integer> stepsCompleted;
    @Column(length = 20)
    private String mood;
    @Column(length = 300)
    private String note;
    @Column(name = "is_skip")
    @Builder.Default
    private boolean isSkip = false;
    @Column(name = "skip_reason", length = 200)
    private String skipReason;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
