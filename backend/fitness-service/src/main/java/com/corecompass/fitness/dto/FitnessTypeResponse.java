package com.corecompass.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FitnessTypeResponse {
    private UUID    id;
    private String  name;
    private String  icon;
    private String  color;
    private String  unit;       // only populated for metric types
    private boolean isSystem;
}