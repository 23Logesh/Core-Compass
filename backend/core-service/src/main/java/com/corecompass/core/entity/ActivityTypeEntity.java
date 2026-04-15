package com.corecompass.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// ACTIVITY TYPE — Type Registry, extensible
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "activity_types", schema = "core_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTypeEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 10)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
