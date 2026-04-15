package com.corecompass.habits.dto;

import lombok.*;
import java.time.Instant;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HabitStackResponse {
    private UUID id; private String name; private String description;
    private List<HabitResponse> habits; private Instant createdAt;
}