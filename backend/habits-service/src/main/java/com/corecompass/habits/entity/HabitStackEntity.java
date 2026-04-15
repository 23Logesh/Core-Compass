package com.corecompass.habits.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "habit_stacks", schema = "habits_schema",
        indexes = @Index(name = "idx_habit_stacks_user", columnList = "user_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitStackEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(length = 200)
    private String description;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "habit_ids", columnDefinition = "jsonb")
    @Builder.Default
    private List<UUID> habitIds = new ArrayList<>();
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
