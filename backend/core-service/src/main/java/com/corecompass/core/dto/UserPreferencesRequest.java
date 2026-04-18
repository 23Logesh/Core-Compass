package com.corecompass.core.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPreferencesRequest {

    @Pattern(regexp = "LIGHT|DARK|SYSTEM",
            message = "theme must be LIGHT, DARK, or SYSTEM")
    private String theme;

    @Size(max = 10)
    private String currency;

    @Size(max = 50)
    private String timezone;

    @Pattern(regexp = "METRIC|IMPERIAL",
            message = "units must be METRIC or IMPERIAL")
    private String units;

    private Boolean weeklyReport;
    private Boolean budgetAlerts;
    private Boolean habitReminders;
}