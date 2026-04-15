package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTypeDTO {
    private UUID id;
    private String name;
    private String icon;
    private String color;
    private String unit;
    private boolean isSystem;
    private boolean isPublic;
}
