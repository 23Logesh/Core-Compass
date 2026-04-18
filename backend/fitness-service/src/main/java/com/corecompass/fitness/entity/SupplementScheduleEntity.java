package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supplement_schedules", schema = "fitness_schema",
        indexes = @Index(name = "idx_supplement_schedules_user",
                columnList = "user_id,is_active"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplementScheduleEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "supplement_type_id", nullable = false)
    private UUID supplementTypeId;

    @Column(name = "dose_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal doseAmount;

    @Column(name = "dose_unit", nullable = false, length = 10)
    @Builder.Default
    private String doseUnit = "GRAM";

    // MORNING | AFTERNOON | EVENING | NIGHT | PRE_WORKOUT | POST_WORKOUT
    @Column(nullable = false, length = 20)
    private String timing;

    // DAILY | MON,WED,FRI etc.
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String frequency = "DAILY";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}