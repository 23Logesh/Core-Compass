package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets", schema = "finance_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;
    @Column(name = "amount_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountLimit;
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
