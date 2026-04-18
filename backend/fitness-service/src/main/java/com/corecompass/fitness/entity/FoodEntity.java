package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "foods", schema = "fitness_schema",
        indexes = @Index(name = "idx_foods_created_by", columnList = "created_by"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FoodEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(name = "calories_per_100g", precision = 8, scale = 2)
    @Builder.Default private BigDecimal caloriesPer100g = BigDecimal.ZERO;

    @Column(name = "protein_per_100g", precision = 6, scale = 2)
    @Builder.Default private BigDecimal proteinPer100g = BigDecimal.ZERO;

    @Column(name = "carbs_per_100g", precision = 6, scale = 2)
    @Builder.Default private BigDecimal carbsPer100g = BigDecimal.ZERO;

    @Column(name = "fat_per_100g", precision = 6, scale = 2)
    @Builder.Default private BigDecimal fatPer100g = BigDecimal.ZERO;

    @Column(name = "serving_size_g", precision = 6, scale = 2)
    private BigDecimal servingSizeG;

    @Column(name = "food_type", nullable = false, length = 10)
    @Builder.Default private String foodType = "SOLID";

    @Column(name = "is_system", nullable = false)
    @Builder.Default private boolean isSystem = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default private Instant updatedAt = Instant.now();

    @PreUpdate void onUpdate() { this.updatedAt = Instant.now(); }
}