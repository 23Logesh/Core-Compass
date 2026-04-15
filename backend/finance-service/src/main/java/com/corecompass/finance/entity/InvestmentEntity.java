package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "investments", schema = "finance_schema",
        indexes = @Index(name = "idx_investments_user", columnList = "user_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "investment_type_id", nullable = false)
    private UUID investmentTypeId;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "invested_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal investedAmount;
    @Column(name = "current_value", precision = 14, scale = 2)
    private BigDecimal currentValue;
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    @Column(columnDefinition = "TEXT")
    private String notes;
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
