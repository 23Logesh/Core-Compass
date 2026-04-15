package com.corecompass.core.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// ═══════════════════════════════════════════════════════════════
// GOAL DTOs
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// TODO DTOs
// ═══════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TodoResponse {
    private UUID      id;
    private UUID      goalId;
    private String    title;
    private String    description;
    private LocalDate dueDate;
    private String    dueTime;
    private boolean   completed;
    private Instant   completedAt;
    private String    recurrenceRule;
    private String    calendarEventId;
    private Instant   createdAt;
    private Instant   updatedAt;
}

// ═══════════════════════════════════════════════════════════════
// MILESTONE DTOs
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// GOAL TYPE DTO (Type Registry)
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// ACTIVITY DTOs
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// DASHBOARD DTO — unified across services via Feign
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// STANDARD API RESPONSE ENVELOPE
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
// PAGINATED RESPONSE
// ═══════════════════════════════════════════════════════════════

