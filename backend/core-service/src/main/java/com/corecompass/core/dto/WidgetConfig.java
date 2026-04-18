package com.corecompass.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One widget entry in the dashboard layout.
 * widgetId examples: GOALS_SUMMARY | TODOS_TODAY | HABITS_SCORE |
 *                    FITNESS_WEEKLY | FINANCE_MONTHLY | NOTIFICATIONS
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WidgetConfig {

    @NotBlank(message = "widgetId is required")
    private String  widgetId;   // e.g. "GOALS_SUMMARY"

    @NotNull
    @Min(0)
    private Integer position;   // 0-based display order

    @NotNull
    private Boolean visible;    // false = hidden but saved
}