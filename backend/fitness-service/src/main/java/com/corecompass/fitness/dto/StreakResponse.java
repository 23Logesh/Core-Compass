package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// ── STREAK ───────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
    private String type;
    private int currentStreak;
    private int bestStreak;
    private int totalActiveDays;
    private LocalDate lastActiveDate;
}
