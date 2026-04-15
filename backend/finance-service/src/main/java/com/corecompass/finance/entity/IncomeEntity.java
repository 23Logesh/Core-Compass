package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "incomes", schema = "finance_schema",
        indexes = @Index(name = "idx_income_user_date", columnList = "user_id,income_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "source_type", nullable = false, length = 60)
    private String sourceType;
    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;
    @Column(length = 200)
    private String note;
    @Column(name = "is_recurring")
    @Builder.Default
    private boolean isRecurring = false;
    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
