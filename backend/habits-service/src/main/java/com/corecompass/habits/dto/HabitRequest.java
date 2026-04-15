package com.corecompass.habits.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.*;
import java.util.*;

@Data
public class HabitRequest {
    @NotBlank @Size(max = 120) private String title;
    @Size(max = 300) private String description;
    @NotBlank private String trackingType;
    @NotBlank private String frequencyPattern;
    private Map<String, Object> frequencyConfig;
    private Double targetValue;
    private String targetUnit;
    private List<String> checklistSteps;
    @Size(max = 200) private String cue;
    @Size(max = 200) private String reward;
    private LocalTime reminderTime;
    private String color; private String icon;
    private UUID categoryTypeId;
    private LocalDate startDate;
}