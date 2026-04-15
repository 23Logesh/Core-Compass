package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "goals",
    schema = "core_schema",
    indexes = {
        @Index(name = "idx_goals_user_status",   columnList = "user_id,status"),
        @Index(name = "idx_goals_user_id",        columnList = "user_id"),
        @Index(name = "idx_goals_category_type",  columnList = "category_type_id")
    }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GoalEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "category_type_id", nullable = false)
    private UUID categoryTypeId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "progress_pct", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPct = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";   // ACTIVE | COMPLETED | ARCHIVED

    @Column(length = 7)
    private String color;               // hex color for UI card

    @Column(length = 100)
    private String icon;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    // One goal has many todos (loaded lazily)
    @OneToMany(mappedBy = "goalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<TodoEntity> todos = new ArrayList<>();

    // One goal has many milestones
    @OneToMany(mappedBy = "goalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<MilestoneEntity> milestones = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }
}
