package com.corecompass.habits.service;
import com.corecompass.habits.dto.*;
import com.corecompass.habits.entity.*;
import com.corecompass.habits.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class HabitsService {
    private final HabitRepository              habitRepo;
    private final HabitCheckinRepository       checkinRepo;
    private final HabitStackRepository         stackRepo;
    private final RoutineGroupRepository       routineRepo;
    private final HabitCategoryTypeRepository  categoryTypeRepo;

    // ── CATEGORY TYPES ──────────────────────────────────────
    public List<HabitCategoryTypeDTO> listCategoryTypes(UUID userId) {
        return categoryTypeRepo.findAvailableForUser(userId).stream().map(c ->
            HabitCategoryTypeDTO.builder().id(c.getId()).name(c.getName())
                .icon(c.getIcon()).color(c.getColor()).isSystem(c.isSystem()).build()
        ).collect(Collectors.toList());
    }

    // ── HABIT CRUD ──────────────────────────────────────────
    @Transactional
    public HabitResponse createHabit(UUID userId, HabitRequest req) {
        HabitEntity e = HabitEntity.builder()
            .userId(userId).title(req.getTitle()).description(req.getDescription())
            .trackingType(req.getTrackingType()).frequencyPattern(req.getFrequencyPattern())
            .frequencyConfig(req.getFrequencyConfig()).targetValue(req.getTargetValue())
            .targetUnit(req.getTargetUnit()).checklistSteps(req.getChecklistSteps())
            .cue(req.getCue()).reward(req.getReward())
            .reminderTime(req.getReminderTime()).color(req.getColor()).icon(req.getIcon())
            .categoryTypeId(req.getCategoryTypeId())
            .startDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now())
            .build();
        e = habitRepo.save(e);
        log.info("Habit created: {} userId={}", e.getTitle(), userId);
        return toResponse(e, null);
    }

    public List<HabitResponse> listHabitsWithTodayStatus(UUID userId) {
        LocalDate today = LocalDate.now();
        return habitRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
            .stream().map(h -> {
                Optional<HabitCheckinEntity> todayCheckin = checkinRepo.findByHabitIdAndCheckinDateAndIsSkipFalse(h.getId(), today);
                return toResponse(h, todayCheckin.orElse(null));
            }).collect(Collectors.toList());
    }

    public HabitResponse getHabit(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        Optional<HabitCheckinEntity> todayCheckin = checkinRepo.findByHabitIdAndCheckinDateAndIsSkipFalse(habitId, LocalDate.now());
        return toResponse(e, todayCheckin.orElse(null));
    }

    @Transactional
    public HabitResponse updateHabit(UUID userId, UUID habitId, HabitRequest req) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        e.setTitle(req.getTitle()); e.setDescription(req.getDescription());
        if (req.getTrackingType()    != null) e.setTrackingType(req.getTrackingType());
        if (req.getFrequencyPattern()!= null) e.setFrequencyPattern(req.getFrequencyPattern());
        if (req.getTargetValue()     != null) e.setTargetValue(req.getTargetValue());
        if (req.getCue()             != null) e.setCue(req.getCue());
        if (req.getReward()          != null) e.setReward(req.getReward());
        if (req.getColor()           != null) e.setColor(req.getColor());
        if (req.getIcon()            != null) e.setIcon(req.getIcon());
        return toResponse(habitRepo.save(e), null);
    }

    @Transactional
    public void deleteHabit(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        e.setDeleted(true);
        habitRepo.save(e);
    }

    @Transactional
    public HabitResponse pauseHabit(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        e.setStatus("PAUSED");
        return toResponse(habitRepo.save(e), null);
    }

    @Transactional
    public HabitResponse resumeHabit(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        e.setStatus("ACTIVE");
        return toResponse(habitRepo.save(e), null);
    }

    // ── CHECK-INS ───────────────────────────────────────────
    @Transactional
    public CheckinResponse checkin(UUID userId, UUID habitId, CheckinRequest req) {
        HabitEntity habit = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        boolean alreadyCheckedIn = checkinRepo.findByHabitIdAndCheckinDate(habitId, date)
                .map(c -> !c.isSkip())  // allow re-checkin if previous was a skip
                .orElse(false);
        if (alreadyCheckedIn) {
            throw new RuntimeException("Already checked in for this date");
        }
    // If there's an existing skip entry, delete it before saving the real checkin
        checkinRepo.findByHabitIdAndCheckinDate(habitId, date)
                .filter(HabitCheckinEntity::isSkip)
                .ifPresent(checkinRepo::delete);


        HabitCheckinEntity checkin = HabitCheckinEntity.builder()
            .habitId(habitId).userId(userId).checkinDate(date)
            .value(req.getValue()).stepsCompleted(req.getStepsCompleted())
            .mood(req.getMood()).note(req.getNote())
            .isSkip(req.isSkip()).skipReason(req.getSkipReason())
            .build();
        checkin = checkinRepo.save(checkin);

        // Update streak (only for non-skip checkins)
        if (!req.isSkip()) {
            updateStreak(habit, date);
        }
        return toCheckinResponse(checkin);
    }

    public Page<CheckinResponse> getCheckinHistory(UUID userId, UUID habitId, Pageable p) {
        habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        return checkinRepo.findByHabitIdOrderByCheckinDateDesc(habitId, p).map(this::toCheckinResponse);
    }

    // ── STREAKS ─────────────────────────────────────────────
    public StreakResponse getStreaks(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        List<HabitCheckinEntity> history = checkinRepo.findByHabitIdOrderByCheckinDateDesc(habitId);
        LocalDate lastDate = history.isEmpty() ? null : history.get(0).getCheckinDate();
        return StreakResponse.builder().habitId(habitId).habitTitle(e.getTitle())
            .currentStreak(e.getCurrentStreak()).bestStreak(e.getBestStreak())
            .totalCheckins(e.getTotalCheckins()).lastCheckinDate(lastDate).build();
    }

    // ── HABIT SCORE (internal Feign) ─────────────────────────
    public int getHabitScore(UUID userId) {
        LocalDate today = LocalDate.now();
        long active    = checkinRepo.countActiveHabits(userId);
        if (active == 0) return 0;
        long checkedIn = checkinRepo.countByUserAndDate(userId, today);
        return (int) Math.min(100, (checkedIn * 100) / active);
    }

    // ── HABIT STACKS ─────────────────────────────────────────
    public List<HabitStackResponse> listStacks(UUID userId) {
        return stackRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
            .stream().map(s -> toStackResponse(userId, s)).collect(Collectors.toList());
    }

    @Transactional
    public HabitStackResponse createStack(UUID userId, HabitStackRequest req) {
        HabitStackEntity e = HabitStackEntity.builder().userId(userId)
            .name(req.getName()).description(req.getDescription())
            .habitIds(req.getHabitIds()).build();
        return toStackResponse(userId, stackRepo.save(e));
    }

    @Transactional
    public HabitStackResponse updateStack(UUID userId, UUID stackId, HabitStackRequest req) {
        HabitStackEntity e = stackRepo.findByIdAndUserId(stackId, userId)
            .orElseThrow(() -> new RuntimeException("Habit stack not found"));
        e.setName(req.getName()); e.setDescription(req.getDescription()); e.setHabitIds(req.getHabitIds());
        return toStackResponse(userId, stackRepo.save(e));
    }

    @Transactional
    public void deleteStack(UUID userId, UUID stackId) {
        stackRepo.findByIdAndUserId(stackId, userId).ifPresent(e -> { e.setDeleted(true); stackRepo.save(e); });
    }

    // ── ROUTINES ─────────────────────────────────────────────
    public List<RoutineGroupResponse> listRoutines(UUID userId) {
        return routineRepo.findByUserIdAndIsDeletedFalseOrderByTimeOfDayAsc(userId)
            .stream().map(r -> toRoutineResponse(userId, r)).collect(Collectors.toList());
    }

    @Transactional
    public RoutineGroupResponse createRoutine(UUID userId, RoutineGroupRequest req) {
        RoutineGroupEntity e = RoutineGroupEntity.builder().userId(userId)
            .name(req.getName()).description(req.getDescription())
            .timeOfDay(req.getTimeOfDay()).habitIds(req.getHabitIds()).build();
        return toRoutineResponse(userId, routineRepo.save(e));
    }

    @Transactional
    public RoutineGroupResponse updateRoutine(UUID userId, UUID routineId, RoutineGroupRequest req) {
        RoutineGroupEntity e = routineRepo.findByIdAndUserId(routineId, userId)
            .orElseThrow(() -> new RuntimeException("Routine not found"));
        e.setName(req.getName()); e.setDescription(req.getDescription());
        e.setTimeOfDay(req.getTimeOfDay()); e.setHabitIds(req.getHabitIds());
        return toRoutineResponse(userId, routineRepo.save(e));
    }

    // ── ROUTINE DELETE ─────────────────────────────────────────────
    @Transactional
    public void deleteRoutine(UUID userId, UUID routineId) {
        RoutineGroupEntity e = routineRepo.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        e.setDeleted(true);
        routineRepo.save(e);
    }

    // ── CATEGORY TYPE CREATE ───────────────────────────────────────
    @Transactional
    public HabitCategoryTypeDTO createCategoryType(UUID userId, HabitCategoryTypeRequest req) {
        HabitCategoryTypeEntity e = HabitCategoryTypeEntity.builder()
                .name(req.getName().trim())
                .icon(req.getIcon())
                .color(req.getColor())
                .isSystem(false)
                .isPublic(false)
                .createdBy(userId)
                .build();
        e = categoryTypeRepo.save(e);
        return HabitCategoryTypeDTO.builder()
                .id(e.getId()).name(e.getName())
                .icon(e.getIcon()).color(e.getColor())
                .isSystem(false).build();
    }

    // ── CATEGORY TYPE UPDATE ───────────────────────────────────────
    @Transactional
    public HabitCategoryTypeDTO updateCategoryType(UUID userId, UUID id, HabitCategoryTypeRequest req) {
        HabitCategoryTypeEntity e = categoryTypeRepo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new RuntimeException("Category type not found or not editable"));
        if (req.getName()  != null) e.setName(req.getName().trim());
        if (req.getIcon()  != null) e.setIcon(req.getIcon());
        if (req.getColor() != null) e.setColor(req.getColor());
        e = categoryTypeRepo.save(e);
        return HabitCategoryTypeDTO.builder()
                .id(e.getId()).name(e.getName())
                .icon(e.getIcon()).color(e.getColor())
                .isSystem(false).build();
    }

    // ── CATEGORY TYPE DELETE ───────────────────────────────────────
    @Transactional
    public void deleteCategoryType(UUID userId, UUID id) {
        HabitCategoryTypeEntity e = categoryTypeRepo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new RuntimeException("Category type not found or not yours to delete"));
        categoryTypeRepo.delete(e);
    }

    // ── HABIT ARCHIVE ──────────────────────────────────────────────
    @Transactional
    public HabitResponse archiveHabit(UUID userId, UUID habitId) {
        HabitEntity e = habitRepo.findByIdAndUserIdAndIsDeletedFalse(habitId, userId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        e.setStatus("ARCHIVED");
        return toResponse(habitRepo.save(e), null);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────
    private void updateStreak(HabitEntity habit, LocalDate date) {
        List<HabitCheckinEntity> history = checkinRepo.findByHabitIdOrderByCheckinDateDesc(habit.getId());
        int streak = 1;
        LocalDate prev = date.minusDays(1);
        for (HabitCheckinEntity c : history) {
            if (c.getCheckinDate().equals(date) || c.isSkip()) continue;
            if (c.getCheckinDate().equals(prev)) { streak++; prev = prev.minusDays(1); }
            else break;
        }
        habit.setCurrentStreak(streak);
        if (streak > habit.getBestStreak()) habit.setBestStreak(streak);
        habit.setTotalCheckins((int) history.stream().filter(c -> !c.isSkip()).count() + 1);
        habitRepo.save(habit);
    }

    private HabitResponse toResponse(HabitEntity e, HabitCheckinEntity todayCheckin) {
        return HabitResponse.builder().id(e.getId()).title(e.getTitle()).description(e.getDescription())
            .trackingType(e.getTrackingType()).frequencyPattern(e.getFrequencyPattern())
            .frequencyConfig(e.getFrequencyConfig()).targetValue(e.getTargetValue())
            .targetUnit(e.getTargetUnit()).checklistSteps(e.getChecklistSteps())
            .cue(e.getCue()).reward(e.getReward()).color(e.getColor()).icon(e.getIcon())
            .currentStreak(e.getCurrentStreak()).bestStreak(e.getBestStreak())
            .totalCheckins(e.getTotalCheckins()).status(e.getStatus()).startDate(e.getStartDate())
            .createdAt(e.getCreatedAt()).checkedInToday(todayCheckin != null)
            .todayCheckin(todayCheckin != null ? toCheckinResponse(todayCheckin) : null).build();
    }
    private CheckinResponse toCheckinResponse(HabitCheckinEntity c) {
        return CheckinResponse.builder().id(c.getId()).habitId(c.getHabitId())
            .checkinDate(c.getCheckinDate()).value(c.getValue()).stepsCompleted(c.getStepsCompleted())
            .mood(c.getMood()).note(c.getNote()).isSkip(c.isSkip()).createdAt(c.getCreatedAt()).build();
    }
    private HabitStackResponse toStackResponse(UUID userId, HabitStackEntity s) {
        List<HabitResponse> habits = s.getHabitIds() != null ? s.getHabitIds().stream()
            .map(id -> habitRepo.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .map(h -> toResponse(h, null)).orElse(null))
            .filter(java.util.Objects::nonNull).collect(Collectors.toList()) : List.of();
        return HabitStackResponse.builder().id(s.getId()).name(s.getName())
            .description(s.getDescription()).habits(habits).createdAt(s.getCreatedAt()).build();
    }
    private RoutineGroupResponse toRoutineResponse(UUID userId, RoutineGroupEntity r) {
        List<HabitResponse> habits = r.getHabitIds() != null ? r.getHabitIds().stream()
            .map(id -> habitRepo.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .map(h -> toResponse(h, null)).orElse(null))
            .filter(java.util.Objects::nonNull).collect(Collectors.toList()) : List.of();
        return RoutineGroupResponse.builder().id(r.getId()).name(r.getName())
            .description(r.getDescription()).timeOfDay(r.getTimeOfDay())
            .habits(habits).createdAt(r.getCreatedAt()).build();
    }
}
