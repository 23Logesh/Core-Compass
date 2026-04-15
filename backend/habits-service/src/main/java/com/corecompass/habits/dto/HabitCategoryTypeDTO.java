package com.corecompass.habits.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HabitCategoryTypeDTO {
    private UUID id; private String name; private String icon; private String color; private boolean isSystem;
}