package com.corecompass.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequest {
    // Optional: override week to generate for (ISO date, e.g. "2026-01-06")
    // If null, defaults to last completed week (Mon-Sun)
    private String weekStart;
}
