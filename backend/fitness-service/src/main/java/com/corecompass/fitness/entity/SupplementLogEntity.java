package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "supplement_logs", schema = "fitness_schema",
        indexes = @Index(name = "idx_supplement_logs_user_date",
                columnList = "user_id,logged_date"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplementLogEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "supplement_type_id", nullable = false)
    private UUID supplementTypeId;

    @Column(name = "dose_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal doseAmount;

    // GRAM | CAPSULE | ML | MG | TABLET | SCOOP
    @Column(name = "dose_unit", nullable = false, length = 10)
    @Builder.Default
    private String doseUnit = "GRAM";

    // MORNING | AFTERNOON | EVENING | NIGHT | PRE_WORKOUT | POST_WORKOUT
    @Column(length = 20)
    private String timing;

    @Column(name = "logged_date", nullable = false)
    @Builder.Default
    private LocalDate loggedDate = LocalDate.now();

    @Column(length = 200)
    private String notes;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}