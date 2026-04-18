package com.corecompass.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "diet_plan_meals", schema = "fitness_schema",
        indexes = @Index(name = "idx_diet_plan_meals_plan", columnList = "plan_id,day_number"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DietPlanMealEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "day_number", nullable = false)
    @Builder.Default private int dayNumber = 1;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    // Optional FK to food library
    @Column(name = "food_id")
    private UUID foodId;

    @Column(name = "food_name", nullable = false, length = 120)
    private String foodName;

    @Column(name = "quantity_g", precision = 6, scale = 2)
    @Builder.Default private BigDecimal quantityG = BigDecimal.valueOf(100);

    @Column(precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_g", precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 6, scale = 2)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 6, scale = 2)
    private BigDecimal fatG;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default private int sortOrder = 0;
}