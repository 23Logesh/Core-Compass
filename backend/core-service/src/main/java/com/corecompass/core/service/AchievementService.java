package com.corecompass.core.service;

import com.corecompass.core.dto.AchievementDTO;
import com.corecompass.core.entity.AchievementDefinitionEntity;
import com.corecompass.core.entity.UserAchievementEntity;
import com.corecompass.core.repository.AchievementDefinitionRepository;
import com.corecompass.core.repository.TodoRepository;
import com.corecompass.core.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementDefinitionRepository definitionRepo;
    private final UserAchievementRepository       userAchievementRepo;
    private final TodoRepository                  todoRepository;
    private final NotificationService             notificationService;

    // ── GET /achievements — all definitions ───────────────────
    public List<AchievementDTO.Definition> listAll() {
        return definitionRepo.findAllByOrderByTitleAsc()
                .stream().map(this::toDefinition).collect(Collectors.toList());
    }

    // ── GET /achievements/earned — user's badges ──────────────
    public List<AchievementDTO.Earned> getEarned(UUID userId) {
        List<UserAchievementEntity> earned = userAchievementRepo
                .findByUserIdOrderByEarnedAtDesc(userId);

        // Fetch definitions in one query
        Set<UUID> defIds = earned.stream()
                .map(UserAchievementEntity::getAchievementId)
                .collect(Collectors.toSet());
        java.util.Map<UUID, AchievementDefinitionEntity> defMap = definitionRepo
                .findAllById(defIds).stream()
                .collect(Collectors.toMap(AchievementDefinitionEntity::getId, d -> d));

        return earned.stream().map(e -> {
            AchievementDefinitionEntity def = defMap.get(e.getAchievementId());
            if (def == null) return null;
            return AchievementDTO.Earned.builder()
                    .achievementId(e.getAchievementId())
                    .key(def.getKey())
                    .title(def.getTitle())
                    .description(def.getDescription())
                    .iconEmoji(def.getIconEmoji())
                    .earnedAt(e.getEarnedAt())
                    .build();
        }).filter(e -> e != null).collect(Collectors.toList());
    }

    // ── GET /achievements/progress — unearned with % ──────────
    public List<AchievementDTO.Progress> getProgress(UUID userId) {
        Set<UUID> earnedIds = userAchievementRepo
                .findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(UserAchievementEntity::getAchievementId)
                .collect(Collectors.toSet());

        // Count todos completed — used for TODO_MACHINE
        long todosCompleted = todoRepository.countByUserIdAndCompletedTrue(userId);

        return definitionRepo.findAllByOrderByTitleAsc().stream()
                .filter(def -> !earnedIds.contains(def.getId()))
                .map(def -> {
                    int current = resolveCurrentValue(def.getConditionType(),
                            def.getConditionValue(), userId,
                            todosCompleted);
                    int pct = Math.min(99,
                            (int) ((current * 100.0) / def.getConditionValue()));
                    return AchievementDTO.Progress.builder()
                            .achievementId(def.getId())
                            .key(def.getKey())
                            .title(def.getTitle())
                            .description(def.getDescription())
                            .iconEmoji(def.getIconEmoji())
                            .conditionValue(def.getConditionValue())
                            .currentValue(current)
                            .progressPct(pct)
                            .build();
                }).collect(Collectors.toList());
    }

    // ── Award an achievement (called internally after key actions) ─
    @Transactional
    public void award(UUID userId, UUID achievementId, String achievementTitle) {
        if (userAchievementRepo.existsByUserIdAndAchievementId(userId, achievementId)) {
            return; // already earned
        }
        UserAchievementEntity record = UserAchievementEntity.builder()
                .userId(userId)
                .achievementId(achievementId)
                .build();
        userAchievementRepo.save(record);

        // Notify the user in-app
        notificationService.createNotification(
                userId,
                "ACHIEVEMENT",
                "🏆 Achievement Unlocked: " + achievementTitle,
                "Congratulations! You just earned the \"" + achievementTitle + "\" badge."
        );
        log.info("Achievement awarded: {} to userId={}", achievementTitle, userId);
    }

    // ── Check TODO_MACHINE after a todo is completed ──────────
    @Transactional
    public void checkTodoMachine(UUID userId) {
        long total = todoRepository.countByUserIdAndCompletedTrue(userId);
        definitionRepo.findAllByOrderByTitleAsc().stream()
                .filter(d -> "TODOS_COMPLETED".equals(d.getConditionType())
                        && total >= d.getConditionValue())
                .forEach(d -> award(userId, d.getId(), d.getTitle()));
    }

    // ── Check GOAL_COMPLETED after a goal is marked done ─────
    @Transactional
    public void checkGoalCompleted(UUID userId) {
        definitionRepo.findAllByOrderByTitleAsc().stream()
                .filter(d -> "GOAL_COMPLETED".equals(d.getConditionType()))
                .forEach(d -> award(userId, d.getId(), d.getTitle()));
    }

    // ── Private helpers ───────────────────────────────────────

    private int resolveCurrentValue(String conditionType, int target,
                                    UUID userId, long todosCompleted) {
        return switch (conditionType) {
            case "TODOS_COMPLETED" -> (int) Math.min(todosCompleted, target - 1);
            // Other conditions (WORKOUT_STREAK, BUDGET_MONTHS etc.) require
            // cross-service data — return 0 as a safe default for progress display.
            // These get awarded via direct award() calls from schedulers.
            default -> 0;
        };
    }

    private AchievementDTO.Definition toDefinition(AchievementDefinitionEntity e) {
        return AchievementDTO.Definition.builder()
                .id(e.getId())
                .key(e.getKey())
                .title(e.getTitle())
                .description(e.getDescription())
                .iconEmoji(e.getIconEmoji())
                .conditionType(e.getConditionType())
                .conditionValue(e.getConditionValue())
                .build();
    }
}