package com.corecompass.fitness.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class MetricTrendsResponse {
    private String metricType;
    private List<MetricTrendPoint> dataPoints;
    private int totalPoints;
}