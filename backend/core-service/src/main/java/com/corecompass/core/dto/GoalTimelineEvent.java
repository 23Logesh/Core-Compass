package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One event in GET /goals/{id}/timeline
 * eventType: TODO_COMPLETED | MILESTONE_COMPLETED | GOAL_CREATED | ACTIVITY_LOGGED
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GoalTimelineEvent {
    private UUID    id;
    private String  eventType;   // TODO_COMPLETED | MILESTONE_COMPLETED | GOAL_CREATED | ACTIVITY_LOGGED
    private String  title;       // todo title, milestone title, or activity type name
    private Instant occurredAt;  // completedAt for todos/milestones, createdAt for others
}