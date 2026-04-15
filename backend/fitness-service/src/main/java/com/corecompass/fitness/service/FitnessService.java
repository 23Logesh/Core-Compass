package com.corecompass.fitness.service;

import com.corecompass.fitness.dto.*;
import com.corecompass.fitness.entity.*;
import com.corecompass.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FitnessService {

    private final CardioLogRepository  cardioRepo;
    private final WorkoutRepository    workoutRepo;
    private final MealLogRepository    mealRepo;
    private final BodyMetricRepository metricRepo;
    private final SleepLogRepository   sleepRepo;
    private final MoodLogRepository    moodRepo;
    private final HydrationRepository  hydrationRepo;
    private final WorkoutExerciseRepository workoutExerciseRepo;

    // ── CARDIO ──────────────────────────────────────────────────
    @Transactional
    public CardioResponse logCardio(UUID userId, CardioRequest req) {
        CardioLogEntity e = CardioLogEntity.builder()
                .userId(userId)
                .cardioType(req.getCardioType().toUpperCase())
                .durationMinutes(req.getDurationMinutes())
                .distanceKm(req.getDistanceKm())
                .caloriesBurned(req.getCaloriesBurned())
                .avgHeartRate(req.getAvgHeartRate())
                .loggedDate(req.getLoggedDate() != null ? req.getLoggedDate() : LocalDate.now())
                .notes(req.getNotes())
                .build();
        log.info("Cardio logged: {} userId={}", e.getCardioType(), userId);
        return toCardioResponse(cardioRepo.save(e));
    }

    public PageResponse<CardioResponse> listCardio(UUID userId, String type, Pageable pageable) {
        if (type != null && !type.isBlank()) {
            return PageResponse.of(
                    cardioRepo.findByUserIdAndCardioTypeAndIsDeletedFalseOrderByLoggedDateDesc(
                                    userId, type.toUpperCase(), pageable)
                            .map(this::toCardioResponse)
            );
        }
        return PageResponse.of(
                cardioRepo.findByUserIdAndIsDeletedFalseOrderByLoggedDateDesc(userId, pageable)
                        .map(this::toCardioResponse)
        );
    }

    @Transactional
    public void deleteCardio(UUID userId, UUID id) {
        cardioRepo.findById(id).ifPresent(e -> {
            if (e.getUserId().equals(userId)) { e.setDeleted(true); cardioRepo.save(e); }
        });
    }

    // ── WORKOUTS ────────────────────────────────────────────────
    @Transactional
    public WorkoutResponse logWorkout(UUID userId, WorkoutRequest req) {
        WorkoutSessionEntity session = WorkoutSessionEntity.builder()
                .userId(userId)
                .workoutName(req.getWorkoutName())
                .sessionDate(req.getSessionDate() != null ? req.getSessionDate() : LocalDate.now())
                .durationMinutes(req.getDurationMinutes())
                .notes(req.getNotes())
                .build();
        session = workoutRepo.save(session);

        // ADD this replacement block:
        List<ExerciseSetResponse> sets = new ArrayList<>();
        if (req.getExerciseSets() != null && !req.getExerciseSets().isEmpty()) {
            WorkoutSessionEntity finalSession = session;
            List<WorkoutExerciseEntity> exerciseEntities = req.getExerciseSets().stream()
                    .map(s -> WorkoutExerciseEntity.builder()
                            .sessionId(finalSession.getId())
                            .exerciseName(s.getExerciseName())
                            .setNumber(s.getSetNumber())
                            .reps(s.getReps())
                            .weightKg(s.getWeightKg())
                            .durationSeconds(s.getDurationSeconds())
                            .build())
                    .collect(Collectors.toList());

            // Actually persist to DB
            List<WorkoutExerciseEntity> savedSets = workoutExerciseRepo.saveAll(exerciseEntities);

            sets = savedSets.stream().map(ex -> ExerciseSetResponse.builder()
                    .exerciseName(ex.getExerciseName())
                    .setNumber(ex.getSetNumber())
                    .reps(ex.getReps())
                    .weightKg(ex.getWeightKg())
                    .build()).collect(Collectors.toList());

            BigDecimal totalVolume = savedSets.stream()
                    .filter(s -> s.getReps() != null && s.getWeightKg() != null)
                    .map(s -> s.getWeightKg().multiply(BigDecimal.valueOf(s.getReps())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            session.setTotalVolumeKg(totalVolume);
            workoutRepo.save(session);
        }

        log.info("Workout logged: {} userId={}", session.getWorkoutName(), userId);
        return toWorkoutResponse(session, sets);
    }

    public PageResponse<WorkoutResponse> listWorkouts(UUID userId, Pageable pageable) {
        return PageResponse.of(
                workoutRepo.findByUserIdAndIsDeletedFalseOrderBySessionDateDesc(userId, pageable)
                        .map(w -> {
                            List<ExerciseSetResponse> sets = workoutExerciseRepo
                                    .findBySessionIdOrderBySetNumberAsc(w.getId())
                                    .stream()
                                    .map(ex -> ExerciseSetResponse.builder()
                                            .exerciseName(ex.getExerciseName())
                                            .setNumber(ex.getSetNumber())
                                            .reps(ex.getReps())
                                            .weightKg(ex.getWeightKg())
                                            .build())
                                    .collect(Collectors.toList());
                            return toWorkoutResponse(w, sets);
                        })
        );
    }

    @Transactional
    public void deleteWorkout(UUID userId, UUID id) {
        workoutRepo.findByIdAndUserId(id, userId).ifPresent(e -> {
            e.setDeleted(true); workoutRepo.save(e);
        });
    }

    // ── MEALS ───────────────────────────────────────────────────
    @Transactional
    public MealResponse logMeal(UUID userId, MealRequest req) {
        MealLogEntity e = MealLogEntity.builder()
                .userId(userId)
                .mealType(req.getMealType().toUpperCase())
                .mealDate(req.getMealDate() != null ? req.getMealDate() : LocalDate.now())
                .mealTime(req.getMealTime())
                .totalCalories(req.getTotalCalories() != null ? req.getTotalCalories().intValue() : null)
                .totalProteinG(req.getProteinG())
                .totalCarbsG(req.getCarbsG())
                .totalFatG(req.getFatG())
                .notes(req.getNotes())
                .build();
        return toMealResponse(mealRepo.save(e));
    }

    public DailyMacroSummary getMealsForDay(UUID userId, LocalDate date) {
        List<MealLogEntity> meals = mealRepo.findByUserIdAndMealDateAndIsDeletedFalse(userId, date);
        List<MealResponse> mealResponses = meals.stream().map(this::toMealResponse).collect(Collectors.toList());
        BigDecimal totalCal  = meals.stream().filter(m -> m.getTotalCalories() != null)
                .map(m -> BigDecimal.valueOf(m.getTotalCalories())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProt = meals.stream().filter(m -> m.getTotalProteinG() != null)
                .map(MealLogEntity::getTotalProteinG).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarb = meals.stream().filter(m -> m.getTotalCarbsG() != null)
                .map(MealLogEntity::getTotalCarbsG).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat  = meals.stream().filter(m -> m.getTotalFatG() != null)
                .map(MealLogEntity::getTotalFatG).reduce(BigDecimal.ZERO, BigDecimal::add);
        return DailyMacroSummary.builder().date(date).totalCalories(totalCal)
                .totalProteinG(totalProt).totalCarbsG(totalCarb).totalFatG(totalFat)
                .meals(mealResponses).build();
    }

    public PageResponse<MealResponse> listMeals(UUID userId, Pageable pageable) {
        return PageResponse.of(
                mealRepo.findByUserIdAndIsDeletedFalse(userId, pageable)
                        .map(this::toMealResponse)
        );
    }

    // ── BODY METRICS ────────────────────────────────────────────
    @Transactional
    public List<BodyMetricResponse> logBodyMetrics(UUID userId, List<BodyMetricRequest> requests) {
        return requests.stream().map(req -> {
            BodyMetricEntity e = BodyMetricEntity.builder()
                    .userId(userId)
                    .metricType(req.getMetricType().toUpperCase())
                    .value(req.getValue())
                    .unit(req.getUnit())
                    .loggedDate(req.getLoggedDate() != null ? req.getLoggedDate() : LocalDate.now())
                    .notes(req.getNotes())
                    .build();
            return toMetricResponse(metricRepo.save(e));
        }).collect(Collectors.toList());
    }

    public PageResponse<BodyMetricResponse> listMetrics(UUID userId, String type, Pageable pageable) {
        return PageResponse.of(
                metricRepo.findByUserIdAndMetricType(userId, type, pageable).map(this::toMetricResponse)
        );
    }

    // ── SLEEP ────────────────────────────────────────────────────
    @Transactional
    public SleepResponse logSleep(UUID userId, SleepRequest req) {
        BigDecimal hours = null;
        if (req.getBedtime() != null && req.getWakeTime() != null) {
            long mins = java.time.Duration.between(req.getBedtime(), req.getWakeTime()).toMinutes();
            if (mins < 0) mins += 1440;
            hours = BigDecimal.valueOf(mins).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
        SleepLogEntity e = SleepLogEntity.builder()
                .userId(userId)
                .sleepDate(req.getSleepDate() != null ? req.getSleepDate() : LocalDate.now())
                .bedtime(req.getBedtime())
                .wakeTime(req.getWakeTime())
                .durationHours(hours)
                .qualityRating(req.getQualityRating())
                .notes(req.getNotes())
                .build();
        return toSleepResponse(sleepRepo.save(e));
    }

    // ── MOOD ──────────────────────────────────────────────────────
    @Transactional
    public MoodResponse logMood(UUID userId, MoodRequest req) {
        MoodLogEntity e = MoodLogEntity.builder()
                .userId(userId).loggedDate(LocalDate.now())
                .mood(req.getMood().toUpperCase()).energyLevel(req.getEnergyLevel()).notes(req.getNotes())
                .build();
        return toMoodResponse(moodRepo.save(e));
    }

    // ── HYDRATION ────────────────────────────────────────────────
    @Transactional
    public HydrationResponse logHydration(UUID userId, HydrationRequest req) {
        HydrationLogEntity e = HydrationLogEntity.builder()
                .userId(userId).loggedDate(LocalDate.now())
                .amountMl(req.getAmountMl()).targetMl(req.getTargetMl())
                .build();
        return toHydrationResponse(hydrationRepo.save(e));
    }

    // ── STREAKS ──────────────────────────────────────────────────
    public List<StreakResponse> getStreaks(UUID userId) {
        List<StreakResponse> streaks = new ArrayList<>();
        int currentStreak = calculateWorkoutStreak(userId);
        int bestStreak    = calculateBestWorkoutStreak(userId);
        List<LocalDate> allDates = workoutRepo.findDistinctWorkoutDates(userId);
        streaks.add(StreakResponse.builder()
                .type("WORKOUT")
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .totalActiveDays(allDates.size())
                .build());
        return streaks;
    }

    private int calculateBestWorkoutStreak(UUID userId) {
        List<LocalDate> dates = workoutRepo.findDistinctWorkoutDates(userId);
        if (dates.isEmpty()) return 0;
        int best = 1, current = 1;
        for (int i = 1; i < dates.size(); i++) {
            // dates are DESC ordered — so previous date should be +1 day from current
            if (dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 1;
            }
        }
        return best;
    }

    // ── WEEKLY SUMMARY ───────────────────────────────────────────
    public FitnessSummaryDTO getWeeklySummary(UUID userId, String weekStart) {
        LocalDate start = LocalDate.parse(weekStart);
        LocalDate end   = start.plusDays(6);
        int workouts    = workoutRepo.countByUserIdAndSessionDateBetweenAndIsDeletedFalse(userId, start, end);
        int calories    = cardioRepo.sumCaloriesBurned(userId, start, end);
        double avgSleep = sleepRepo.avgDurationHours(userId, start, end);
        int streak      = calculateWorkoutStreak(userId);
        return FitnessSummaryDTO.builder()
                .workoutsThisWeek(workouts).caloriesBurnedThisWeek(calories)
                .avgSleepHours(avgSleep).currentStreak(streak).build();
    }

    // Adapter for Feign calls from Report + Core services
    public FitnessSummaryDTO getWeeklySummaryForFeign(UUID userId, String weekStart) {
        return getWeeklySummary(userId, weekStart);
    }

    // ── PRIVATE ──────────────────────────────────────────────────
    private int calculateWorkoutStreak(UUID userId) {
        List<LocalDate> dates = workoutRepo.findDistinctWorkoutDates(userId);
        if (dates.isEmpty()) return 0;
        int streak = 0;
        LocalDate check = LocalDate.now();
        for (LocalDate d : dates) {
            if (d.equals(check) || d.equals(check.minusDays(1))) { streak++; check = d.minusDays(1); }
            else break;
        }
        return streak;
    }

    private CardioResponse toCardioResponse(CardioLogEntity e) {
        return CardioResponse.builder().id(e.getId()).cardioType(e.getCardioType())
                .durationMinutes(e.getDurationMinutes()).distanceKm(e.getDistanceKm())
                .caloriesBurned(e.getCaloriesBurned()).loggedDate(e.getLoggedDate())
                .createdAt(e.getCreatedAt()).build();
    }
    private WorkoutResponse toWorkoutResponse(WorkoutSessionEntity e, List<ExerciseSetResponse> sets) {
        return WorkoutResponse.builder().id(e.getId()).workoutName(e.getWorkoutName())
                .sessionDate(e.getSessionDate()).durationMinutes(e.getDurationMinutes())
                .totalVolumeKg(e.getTotalVolumeKg()).notes(e.getNotes())
                .exerciseSets(sets).createdAt(e.getCreatedAt()).build();
    }
    private MealResponse toMealResponse(MealLogEntity e) {
        return MealResponse.builder().id(e.getId()).mealType(e.getMealType())
                .mealDate(e.getMealDate()).mealTime(e.getMealTime())
                .totalCalories(e.getTotalCalories() != null ? BigDecimal.valueOf(e.getTotalCalories()) : null)
                .proteinG(e.getTotalProteinG()).carbsG(e.getTotalCarbsG()).fatG(e.getTotalFatG())
                .createdAt(e.getCreatedAt()).build();
    }
    private SleepResponse toSleepResponse(SleepLogEntity e) {
        return SleepResponse.builder().id(e.getId()).sleepDate(e.getSleepDate())
                .bedtime(e.getBedtime() != null ? e.getBedtime().toString() : null)
                .wakeTime(e.getWakeTime() != null ? e.getWakeTime().toString() : null)
                .durationHours(e.getDurationHours()).qualityRating(e.getQualityRating())
                .createdAt(e.getCreatedAt()).build();
    }
    private MoodResponse toMoodResponse(MoodLogEntity e) {
        return MoodResponse.builder().id(e.getId()).mood(e.getMood())
                .energyLevel(e.getEnergyLevel()).loggedDate(e.getLoggedDate()).createdAt(e.getCreatedAt()).build();
    }
    private HydrationResponse toHydrationResponse(HydrationLogEntity e) {
        return HydrationResponse.builder().id(e.getId()).amountMl(e.getAmountMl())
                .targetMl(e.getTargetMl()).loggedDate(e.getLoggedDate()).createdAt(e.getCreatedAt()).build();
    }
    private BodyMetricResponse toMetricResponse(BodyMetricEntity e) {
        return BodyMetricResponse.builder().id(e.getId()).metricType(e.getMetricType())
                .value(e.getValue()).unit(e.getUnit()).loggedDate(e.getLoggedDate()).createdAt(e.getCreatedAt()).build();
    }
}