package com.corecompass.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses", schema = "finance_schema",
        indexes = {@Index(name = "idx_exp_user_date", columnList = "user_id,expense_date"),
                @Index(name = "idx_exp_user_cat", columnList = "user_id,category_id")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEntity {
    @Id
    @UuidGenerator
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
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    @Column(length = 100)
    private String merchant;
    @Column(length = 200)
    private String note;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;
    @Column(name = "is_recurring")
    @Builder.Default
    private boolean isRecurring = false;
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
