package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Type Registry Pattern — goal category types.
 * System types (isSystem=true) ship with the app.
 * Users create custom types (isSystem=false).
 * isPublic=true shares to community pool.
 */
@Entity
@Table(
    name = "goal_types",
    schema = "core_schema",
    indexes = {
        @Index(name = "idx_goal_types_created_by", columnList = "created_by")
    }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GoalTypeEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 10)
    private String icon;       // emoji or icon key

    @Column(length = 7)
    private String color;      // hex #RRGGBB

    @Column(length = 50)
    private String unit;       // optional unit label

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @Column(name = "created_by")
    private UUID createdBy;    // null for system types

    // JSON free-form metadata (type-specific extra fields)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }
}
