package com.corecompass.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPreferencesResponse {
    private String  theme;
    private String  currency;
    private String  timezone;
    private String  units;
    private boolean weeklyReport;
    private boolean budgetAlerts;
    private boolean habitReminders;
}