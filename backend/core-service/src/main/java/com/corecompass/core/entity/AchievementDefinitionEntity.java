package com.corecompass.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "achievement_definitions", schema = "core_schema")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AchievementDefinitionEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String key;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(name = "icon_emoji", nullable = false, length = 10)
    @Builder.Default
    private String iconEmoji = "🏆";

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType;

    @Column(name = "condition_value", nullable = false)
    private int conditionValue;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}