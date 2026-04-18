package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "diet_plans", schema = "fitness_schema",
        indexes = @Index(name = "idx_diet_plans_user", columnList = "user_id,is_active"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DietPlanEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false, length = 20)
    @Builder.Default private String goal = "MAINTENANCE";

    @Column(name = "daily_calorie_target", precision = 8, scale = 2)
    private BigDecimal dailyCalorieTarget;

    @Column(name = "daily_protein_g", precision = 6, scale = 2)
    private BigDecimal dailyProteinG;

    @Column(name = "daily_carbs_g", precision = 6, scale = 2)
    private BigDecimal dailyCarbsG;

    @Column(name = "daily_fat_g", precision = 6, scale = 2)
    private BigDecimal dailyFatG;

    @Column(name = "is_active", nullable = false)
    @Builder.Default private boolean isActive = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default private Instant updatedAt = Instant.now();

    @PreUpdate void onUpdate() { this.updatedAt = Instant.now(); }
}