package com.corecompass.core.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ActivityTypeDTO {
    private UUID    id;
    private String  name;
    private String  icon;
    private String  color;
    private boolean isSystem;
    private Instant createdAt;
}