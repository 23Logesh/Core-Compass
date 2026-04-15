package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// MEAL LOG  (FIX: was missing; field names match V3 SQL schema)
// SQL cols: meal_type, meal_date, meal_time, total_calories,
//           total_protein_g, total_carbs_g, total_fat_g, notes
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "meal_logs", schema = "fitness_schema",
        indexes = @Index(name = "idx_meal_user_date", columnList = "user_id,meal_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLogEntity {
    @Id
    @UuidGenerator
    @Column(updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    // BREAKFAST | LUNCH | DINNER | SNACK
    @Column(name = "meal_type", nullable = false, length = 30)
    private String mealType;
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;
    @Column(name = "meal_time")
    private LocalTime mealTime;
    @Column(name = "total_calories", columnDefinition = "NUMERIC")
    private Integer totalCalories;
    @Column(name = "total_protein_g", precision = 6, scale = 1)
    private BigDecimal totalProteinG;
    @Column(name = "total_carbs_g", precision = 6, scale = 1)
    private BigDecimal totalCarbsG;
    @Column(name = "total_fat_g", precision = 6, scale = 1)
    private BigDecimal totalFatG;
    private String notes;
    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
