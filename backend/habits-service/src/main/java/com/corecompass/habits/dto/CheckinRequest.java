package com.corecompass.habits.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CheckinRequest {
    private LocalDate date;
    private Double value;
    private List<Integer> stepsCompleted;
    @Pattern(regexp = "GREAT|GOOD|NEUTRAL|TIRED|SICK") private String mood;
    @Size(max = 300) private String note;
    private boolean isSkip;
    @Size(max = 200) private String skipReason;
}