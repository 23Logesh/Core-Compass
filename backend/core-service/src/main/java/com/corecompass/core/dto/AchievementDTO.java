package com.corecompass.core.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

public class AchievementDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Definition {
        private UUID   id;
        private String key;
        private String title;
        private String description;
        private String iconEmoji;
        private String conditionType;
        private int    conditionValue;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Earned {
        private UUID   achievementId;
        private String key;
        private String title;
        private String description;
        private String iconEmoji;
        private Instant earnedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Progress {
        private UUID   achievementId;
        private String key;
        private String title;
        private String description;
        private String iconEmoji;
        private int    conditionValue;  // target
        private int    currentValue;    // user's current count
        private int    progressPct;     // 0-99 (100 means earned)
    }
}