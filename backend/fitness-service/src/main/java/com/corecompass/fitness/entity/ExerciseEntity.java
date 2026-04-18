package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exercises", schema = "fitness_schema",
        indexes = {
                @Index(name = "idx_exercises_muscle",     columnList = "muscle_group"),
                @Index(name = "idx_exercises_created_by", columnList = "created_by")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExerciseEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "muscle_group", nullable = false, length = 30)
    private String muscleGroup;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String equipment = "NONE";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String difficulty = "BEGINNER";

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;

    // null for system exercises
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}