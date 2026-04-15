package com.corecompass.finance.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity @Table(name="savings_goals",schema="finance_schema")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SavingsGoalEntity {
    @Id @UuidGenerator private UUID id;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(nullable=false,length=120) private String title;
    @Column(name="target_amount",nullable=false,precision=12,scale=2) private BigDecimal targetAmount;
    @Column(name="current_amount",precision=12,scale=2) @Builder.Default private BigDecimal currentAmount=BigDecimal.ZERO;
    @Column(name="target_date") private LocalDate targetDate;
    @Column(name="is_deleted") @Builder.Default private boolean isDeleted=false;
    @Column(name="created_at",updatable=false) @Builder.Default private Instant createdAt=Instant.now();
    @Column(name="updated_at") @Builder.Default private Instant updatedAt=Instant.now();
    @PreUpdate void onUpdate(){updatedAt=Instant.now();}
}

