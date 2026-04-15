package com.corecompass.habits.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Data
public class HabitStackRequest {
    @NotBlank @Size(max = 80) private String name;
    @Size(max = 200) private String description;
    @NotNull private List<UUID> habitIds;
}