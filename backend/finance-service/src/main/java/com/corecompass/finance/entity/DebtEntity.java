package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "debts", schema = "finance_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "debt_type", length = 50)
    private String debtType;
    @Column(name = "principal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmount;
    @Column(name = "current_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentBalance;
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;
    @Column(name = "min_payment", precision = 10, scale = 2)
    private BigDecimal minPayment;
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
