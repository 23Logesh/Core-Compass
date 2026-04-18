package com.corecompass.fitness.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SupplementTypeResponse {
    private UUID    id;
    private String  name;
    private String  category;
    private String  description;
    private boolean isSystem;
}