package com.corecompass.habits.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Data
public class RoutineGroupRequest {
    @NotBlank @Size(max = 80) private String name;
    @Size(max = 200) private String description;
    @Pattern(regexp = "MORNING|AFTERNOON|EVENING|NIGHT") private String timeOfDay;
    @NotNull private List<UUID> habitIds;
}