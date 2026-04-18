package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recurring_expenses", schema = "finance_schema",
        indexes = @Index(name = "idx_rec_exp_user", columnList = "user_id,is_active"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RecurringExpenseEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "sub_category_id")
    private UUID subCategoryId;

    @Column(name = "payment_method_id")
    private UUID paymentMethodId;

    @Column(length = 100)
    private String merchant;

    @Column(length = 200)
    private String note;

    // DAILY | WEEKLY | MONTHLY | YEARLY
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String frequency = "MONTHLY";

    // Day of month (1-31) for MONTHLY; day of week (1-7) for WEEKLY
    @Column(name = "day_of_period")
    private Integer dayOfPeriod;

    @Column(name = "starts_on")
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}