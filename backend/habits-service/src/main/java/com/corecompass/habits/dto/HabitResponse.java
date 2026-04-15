package com.corecompass.habits.dto;

import lombok.*;
import java.time.*;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HabitResponse {
    private UUID id; private String title; private String description;
    private String trackingType; private String frequencyPattern;
    private Map<String, Object> frequencyConfig;
    private Double targetValue; private String targetUnit;
    private List<String> checklistSteps;
    private String cue; private String reward; private String color; private String icon;
    private int currentStreak; private int bestStreak; private int totalCheckins;
    private String status; private LocalDate startDate; private Instant createdAt;
    private boolean checkedInToday; private CheckinResponse todayCheckin;
}