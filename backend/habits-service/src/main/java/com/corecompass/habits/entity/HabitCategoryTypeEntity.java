package com.corecompass.habits.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "habit_category_types", schema = "habits_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitCategoryTypeEntity {
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
    @Column(name = "is_system")
    @Builder.Default
    private boolean isSystem = false;
    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = false;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
