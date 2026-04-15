package com.corecompass.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expense_categories", schema = "finance_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryEntity {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(nullable = false, length = 60)
    private String name;
    @Column(length = 10)
    private String icon;
    @Column(length = 7)
    private String color;
    @Column(name = "parent_id")
    private UUID parentId;
    @Column(name = "is_system")
    @Builder.Default
    private boolean isSystem = false;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
