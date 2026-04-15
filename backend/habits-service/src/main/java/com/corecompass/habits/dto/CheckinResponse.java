package com.corecompass.habits.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CheckinResponse {
    private UUID id; private UUID habitId; private LocalDate checkinDate;
    private Double value; private List<Integer> stepsCompleted;
    private String mood; private String note; private boolean isSkip; private Instant createdAt;
}