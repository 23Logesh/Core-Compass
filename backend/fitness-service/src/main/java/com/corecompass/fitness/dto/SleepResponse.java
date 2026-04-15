package com.corecompass.fitness.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor public class SleepResponse {
    private UUID id; private LocalDate sleepDate; private String bedtime;
    private String wakeTime; private BigDecimal durationHours;
    private Integer qualityRating; private Instant createdAt;
}

