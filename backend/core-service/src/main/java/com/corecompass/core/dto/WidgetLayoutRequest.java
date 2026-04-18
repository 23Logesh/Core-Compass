package com.corecompass.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class WidgetLayoutRequest {

    @NotEmpty(message = "widgets list cannot be empty")
    @Size(max = 20, message = "Maximum 20 widgets allowed")
    @Valid
    private List<WidgetConfig> widgets;
}