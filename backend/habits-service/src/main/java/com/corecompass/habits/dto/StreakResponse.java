package com.corecompass.habits.dto;
import lombok.*;
import java.time.*;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StreakResponse {
    private UUID habitId; private String habitTitle;
    private int currentStreak; private int bestStreak; private int totalCheckins; private LocalDate lastCheckinDate;
}

