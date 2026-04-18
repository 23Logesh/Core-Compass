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

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    private final FitnessTargetRepository targetRepo;

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
        List<ExerciseSetResponse> sets = persistExerciseSets(session.getId(), req.getExerciseSets(), session);
        workoutRepo.save(session); // save updated totalVolumeKg
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

    // ── PUT /fitness/cardio/{id} ──────────────────────────────────
    @Transactional
    public CardioResponse updateCardio(UUID userId, UUID id, CardioUpdateRequest req) {
        CardioLogEntity e = cardioRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cardio log not found"));
        if (req.getCardioType()      != null) e.setCardioType(req.getCardioType().toUpperCase());
        if (req.getDurationMinutes() != null) e.setDurationMinutes(req.getDurationMinutes());
        if (req.getDistanceKm()      != null) e.setDistanceKm(req.getDistanceKm());
        if (req.getCaloriesBurned()  != null) e.setCaloriesBurned(req.getCaloriesBurned());
        if (req.getAvgHeartRate()    != null) e.setAvgHeartRate(req.getAvgHeartRate());
        if (req.getLoggedDate()      != null) e.setLoggedDate(req.getLoggedDate());
        if (req.getNotes()           != null) e.setNotes(req.getNotes());
        return toCardioResponse(cardioRepo.save(e));
    }

    // ── PUT /fitness/workouts/{id} ────────────────────────────────
    @Transactional
    public WorkoutResponse updateWorkout(UUID userId, UUID id, WorkoutUpdateRequest req) {
        WorkoutSessionEntity session = workoutRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
        if (req.getWorkoutName()     != null) session.setWorkoutName(req.getWorkoutName());
        if (req.getSessionDate()     != null) session.setSessionDate(req.getSessionDate());
        if (req.getDurationMinutes() != null) session.setDurationMinutes(req.getDurationMinutes());
        if (req.getNotes()           != null) session.setNotes(req.getNotes());

        List<ExerciseSetResponse> sets;
        if (req.getExerciseSets() != null) {
            workoutExerciseRepo.deleteBySessionId(id);
            sets = persistExerciseSets(id, req.getExerciseSets(), session);
        } else {
            sets = loadSets(id);
        }
        return toWorkoutResponse(workoutRepo.save(session), sets);
    }

    // ── PUT /fitness/meals/{id} ───────────────────────────────────
    @Transactional
    public MealResponse updateMeal(UUID userId, UUID id, MealUpdateRequest req) {
        MealLogEntity e = mealRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal log not found"));
        if (req.getMealType()      != null) e.setMealType(req.getMealType().toUpperCase());
        if (req.getMealDate()      != null) e.setMealDate(req.getMealDate());
        if (req.getMealTime()      != null) e.setMealTime(req.getMealTime());
        if (req.getTotalCalories() != null) e.setTotalCalories(req.getTotalCalories().intValue());
        if (req.getProteinG()      != null) e.setTotalProteinG(req.getProteinG());
        if (req.getCarbsG()        != null) e.setTotalCarbsG(req.getCarbsG());
        if (req.getFatG()          != null) e.setTotalFatG(req.getFatG());
        if (req.getNotes()         != null) e.setNotes(req.getNotes());
        return toMealResponse(mealRepo.save(e));
    }

    // ── PUT /fitness/metrics/{id} ─────────────────────────────────
    @Transactional
    public BodyMetricResponse updateMetric(UUID userId, UUID id, BodyMetricUpdateRequest req) {
        BodyMetricEntity e = metricRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metric not found"));
        if (req.getValue()      != null) e.setValue(req.getValue());
        if (req.getUnit()       != null) e.setUnit(req.getUnit());
        if (req.getLoggedDate() != null) e.setLoggedDate(req.getLoggedDate());
        if (req.getNotes()      != null) e.setNotes(req.getNotes());
        return toMetricResponse(metricRepo.save(e));
    }

    // ── GET /fitness/sleep ────────────────────────────────────────
    public PageResponse<SleepResponse> listSleep(UUID userId, Pageable pageable) {
        return PageResponse.of(
                sleepRepo.findByUserIdOrderBySleepDateDesc(userId, pageable).map(this::toSleepResponse));
    }

    // ── PUT /fitness/sleep/{id} ───────────────────────────────────
    @Transactional
    public SleepResponse updateSleep(UUID userId, UUID id, SleepUpdateRequest req) {
        SleepLogEntity e = sleepRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleep log not found"));
        if (req.getSleepDate()     != null) e.setSleepDate(req.getSleepDate());
        if (req.getBedtime()       != null) e.setBedtime(req.getBedtime());
        if (req.getWakeTime()      != null) e.setWakeTime(req.getWakeTime());
        if (req.getQualityRating() != null) e.setQualityRating(req.getQualityRating());
        if (req.getNotes()         != null) e.setNotes(req.getNotes());
        if (e.getBedtime() != null && e.getWakeTime() != null) {
            long mins = java.time.Duration.between(e.getBedtime(), e.getWakeTime()).toMinutes();
            if (mins < 0) mins += 1440;
            e.setDurationHours(BigDecimal.valueOf(mins).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        }
        return toSleepResponse(sleepRepo.save(e));
    }

    // ── GET /fitness/hydration/history ────────────────────────────
    public PageResponse<HydrationResponse> listHydration(UUID userId, Pageable pageable) {
        return PageResponse.of(
                hydrationRepo.findByUserIdOrderByLoggedDateDesc(userId, pageable).map(this::toHydrationResponse));
    }

    // ── PUT /fitness/hydration/{id} ───────────────────────────────
    @Transactional
    public HydrationResponse updateHydration(UUID userId, UUID id, HydrationUpdateRequest req) {
        HydrationLogEntity e = hydrationRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hydration log not found"));
        if (req.getAmountMl() != null) e.setAmountMl(req.getAmountMl());
        if (req.getTargetMl() != null) e.setTargetMl(req.getTargetMl());
        return toHydrationResponse(hydrationRepo.save(e));
    }

    // ── GET /fitness/mood ─────────────────────────────────────────
    public PageResponse<MoodResponse> listMood(UUID userId, Pageable pageable) {
        return PageResponse.of(
                moodRepo.findByUserIdOrderByLoggedDateDesc(userId, pageable).map(this::toMoodResponse));
    }

    // ── PUT /fitness/mood/{id} ────────────────────────────────────
    @Transactional
    public MoodResponse updateMood(UUID userId, UUID id, MoodUpdateRequest req) {
        MoodLogEntity e = moodRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mood log not found"));
        if (req.getMood()        != null) e.setMood(req.getMood().toUpperCase());
        if (req.getEnergyLevel() != null) e.setEnergyLevel(req.getEnergyLevel());
        if (req.getNotes()       != null) e.setNotes(req.getNotes());
        return toMoodResponse(moodRepo.save(e));
    }

    // ── GET /fitness/metrics/stats ────────────────────────────────
    public BodyMetricStatsResponse getMetricStats(UUID userId) {
        Pageable one = PageRequest.of(0, 1);
        List<BodyMetricEntity> wList = metricRepo.findLatestByUserIdAndMetricType(userId, "WEIGHT", one);
        List<BodyMetricEntity> hList = metricRepo.findLatestByUserIdAndMetricType(userId, "HEIGHT", one);

        BigDecimal weightKg = wList.isEmpty() ? null : normaliseWeight(wList.get(0));
        BigDecimal heightCm = hList.isEmpty() ? null : normaliseHeight(hList.get(0));

        BigDecimal bmi = null;
        String bmiCategory = null;
        BigDecimal tdeeKcal = null;

        if (weightKg != null && heightCm != null) {
            BigDecimal hM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            bmi = weightKg.divide(hM.multiply(hM), 1, RoundingMode.HALF_UP);
            double d = bmi.doubleValue();
            bmiCategory = d < 18.5 ? "Underweight" : d < 25.0 ? "Normal" : d < 30.0 ? "Overweight" : "Obese";
            // Mifflin-St Jeor base without age/gender (30yr male assumption for TDEE estimate)
            BigDecimal bmrBase = BigDecimal.valueOf(10).multiply(weightKg)
                    .add(BigDecimal.valueOf(6.25).multiply(heightCm))
                    .subtract(BigDecimal.valueOf(150)).add(BigDecimal.valueOf(5));
            tdeeKcal = bmrBase.multiply(BigDecimal.valueOf(1.55)).setScale(0, RoundingMode.HALF_UP);
        }

        return BodyMetricStatsResponse.builder()
                .latestWeightKg(weightKg)
                .latestHeightCm(heightCm)
                .bmi(bmi).bmiCategory(bmiCategory).tdeeKcal(tdeeKcal)
                .weightDate(wList.isEmpty() ? null : wList.get(0).getLoggedDate())
                .heightDate(hList.isEmpty() ? null : hList.get(0).getLoggedDate())
                .build();
    }

    // ── GET /fitness/metrics/trends ───────────────────────────────
    public MetricTrendsResponse getMetricTrends(UUID userId, String type, int days) {
        List<BodyMetricEntity> raw = metricRepo.findTrendData(userId, type.toUpperCase(), LocalDate.now().minusDays(days));
        List<MetricTrendPoint> points = raw.stream()
                .map(e -> MetricTrendPoint.builder().date(e.getLoggedDate()).value(e.getValue()).unit(e.getUnit()).build())
                .collect(Collectors.toList());
        return MetricTrendsResponse.builder().metricType(type.toUpperCase()).dataPoints(points).totalPoints(points.size()).build();
    }

    // ── GET /fitness/summary/monthly ─────────────────────────────
    public MonthlySummaryDTO getMonthlySummary(UUID userId, String month) {
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        int yr = ym.getYear(), mo = ym.getMonthValue();

        List<String> moods = moodRepo.findMoodsForMonth(userId, yr, mo);
        double avgMood = moods.stream().mapToInt(m -> switch (m.toUpperCase()) {
            case "GREAT" -> 5; case "GOOD" -> 4; case "NEUTRAL" -> 3; case "TIRED" -> 2; default -> 1;
        }).average().orElse(0);

        long totalCalIn = mealRepo.sumCaloriesForMonth(userId, yr, mo);
        long activeDays = mealRepo.countDistinctDaysForMonth(userId, yr, mo);
        BigDecimal avgDailyCal = activeDays > 0
                ? BigDecimal.valueOf(totalCalIn).divide(BigDecimal.valueOf(activeDays), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal vol = workoutRepo.sumVolumeForMonth(userId, yr, mo);

        return MonthlySummaryDTO.builder()
                .year(yr).month(mo)
                .totalWorkouts((int) workoutRepo.countForMonth(userId, yr, mo))
                .totalCardioSessions((int) cardioRepo.countForMonth(userId, yr, mo))
                .totalCaloriesBurned(cardioRepo.sumCaloriesForMonth(userId, yr, mo))
                .totalVolumeKg(vol != null ? vol : BigDecimal.ZERO)
                .avgSleepHours(BigDecimal.valueOf(sleepRepo.avgDurationHoursForMonth(userId, yr, mo)).setScale(1, RoundingMode.HALF_UP))
                .avgMoodScore(BigDecimal.valueOf(avgMood).setScale(1, RoundingMode.HALF_UP))
                .totalHydrationMl(hydrationRepo.sumAmountForMonth(userId, yr, mo))
                .avgDailyCalories(avgDailyCal)
                .workoutStreakEnd(calculateWorkoutStreak(userId))
                .build();
    }

    // ── GET /fitness/targets ─────────────────────────────────────
    public FitnessTargetResponse getTargets(UUID userId) {
        return targetRepo.findByUserId(userId).map(e -> FitnessTargetResponse.builder()
                        .id(e.getId()).weeklyWorkoutTarget(e.getWeeklyWorkoutTarget())
                        .dailyCalorieTarget(e.getDailyCalorieTarget()).dailyProteinTargetG(e.getDailyProteinTargetG())
                        .dailyHydrationTargetMl(e.getDailyHydrationTargetMl()).dailyCalorieBurnTarget(e.getDailyCalorieBurnTarget())
                        .updatedAt(e.getUpdatedAt()).build())
                .orElseGet(() -> FitnessTargetResponse.builder().build());
    }

    // ── PUT /fitness/targets ──────────────────────────────────────
    @Transactional
    public FitnessTargetResponse upsertTargets(UUID userId, FitnessTargetRequest req) {
        FitnessTargetEntity e = targetRepo.findByUserId(userId)
                .orElseGet(() -> FitnessTargetEntity.builder().userId(userId).build());
        if (req.getWeeklyWorkoutTarget()     != null) e.setWeeklyWorkoutTarget(req.getWeeklyWorkoutTarget());
        if (req.getDailyCalorieTarget()      != null) e.setDailyCalorieTarget(req.getDailyCalorieTarget());
        if (req.getDailyProteinTargetG()     != null) e.setDailyProteinTargetG(req.getDailyProteinTargetG());
        if (req.getDailyHydrationTargetMl()  != null) e.setDailyHydrationTargetMl(req.getDailyHydrationTargetMl());
        if (req.getDailyCalorieBurnTarget()  != null) e.setDailyCalorieBurnTarget(req.getDailyCalorieBurnTarget());
        FitnessTargetEntity saved = targetRepo.save(e);
        return FitnessTargetResponse.builder().id(saved.getId()).weeklyWorkoutTarget(saved.getWeeklyWorkoutTarget())
                .dailyCalorieTarget(saved.getDailyCalorieTarget()).dailyProteinTargetG(saved.getDailyProteinTargetG())
                .dailyHydrationTargetMl(saved.getDailyHydrationTargetMl())
                .dailyCalorieBurnTarget(saved.getDailyCalorieBurnTarget()).updatedAt(saved.getUpdatedAt()).build();
    }

    // ── GET /fitness/workouts/prs ─────────────────────────────────
    public List<WorkoutPRResponse> getWorkoutPRs(UUID userId) {
        List<WorkoutExerciseEntity> all = workoutExerciseRepo.findAllByUserId(userId);
        Map<String, List<WorkoutExerciseEntity>> grouped = all.stream()
                .collect(Collectors.groupingBy(e -> e.getExerciseName().toLowerCase()));
        return grouped.entrySet().stream().map(entry -> {
            List<WorkoutExerciseEntity> sets = entry.getValue();
            String name = sets.get(0).getExerciseName();
            WorkoutExerciseEntity maxWt = sets.stream().filter(s -> s.getWeightKg() != null)
                    .max(Comparator.comparing(WorkoutExerciseEntity::getWeightKg)).orElse(null);
            WorkoutExerciseEntity maxRp = sets.stream().filter(s -> s.getReps() != null)
                    .max(Comparator.comparing(WorkoutExerciseEntity::getReps)).orElse(null);
            WorkoutExerciseEntity maxVol = sets.stream().filter(s -> s.getWeightKg() != null && s.getReps() != null)
                    .max(Comparator.comparing(s -> s.getWeightKg().multiply(BigDecimal.valueOf(s.getReps())))).orElse(null);
            BigDecimal vol = maxVol != null ? maxVol.getWeightKg().multiply(BigDecimal.valueOf(maxVol.getReps())) : null;
            LocalDate achieved = maxVol != null
                    ? workoutRepo.findById(maxVol.getSessionId()).map(WorkoutSessionEntity::getSessionDate).orElse(null)
                    : null;
            return WorkoutPRResponse.builder().exerciseName(name)
                    .maxWeightKg(maxWt != null ? maxWt.getWeightKg() : null)
                    .maxReps(maxRp != null ? maxRp.getReps() : null)
                    .maxVolumeKg(vol).achievedOn(achieved).build();
        }).sorted(Comparator.comparing(WorkoutPRResponse::getExerciseName)).collect(Collectors.toList());
    }

    // ── private helpers (ADD these, they're NEW) ─────────────────
    private List<ExerciseSetResponse> persistExerciseSets(UUID sessionId, List<ExerciseSetRequest> requests, WorkoutSessionEntity session) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        List<WorkoutExerciseEntity> entities = requests.stream()
                .map(s -> WorkoutExerciseEntity.builder().sessionId(sessionId)
                        .exerciseName(s.getExerciseName()).setNumber(s.getSetNumber())
                        .reps(s.getReps()).weightKg(s.getWeightKg()).durationSeconds(s.getDurationSeconds()).build())
                .collect(Collectors.toList());
        List<WorkoutExerciseEntity> saved = workoutExerciseRepo.saveAll(entities);
        BigDecimal totalVolume = saved.stream()
                .filter(s -> s.getReps() != null && s.getWeightKg() != null)
                .map(s -> s.getWeightKg().multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setTotalVolumeKg(totalVolume);
        return saved.stream().map(ex -> ExerciseSetResponse.builder()
                .exerciseName(ex.getExerciseName()).setNumber(ex.getSetNumber())
                .reps(ex.getReps()).weightKg(ex.getWeightKg()).build()).collect(Collectors.toList());
    }

    private List<ExerciseSetResponse> loadSets(UUID sessionId) {
        return workoutExerciseRepo.findBySessionIdOrderBySetNumberAsc(sessionId).stream()
                .map(ex -> ExerciseSetResponse.builder().exerciseName(ex.getExerciseName())
                        .setNumber(ex.getSetNumber()).reps(ex.getReps()).weightKg(ex.getWeightKg()).build())
                .collect(Collectors.toList());
    }

    private BigDecimal normaliseWeight(BodyMetricEntity e) {
        if ("lbs".equalsIgnoreCase(e.getUnit()))
            return e.getValue().multiply(BigDecimal.valueOf(0.453592)).setScale(2, RoundingMode.HALF_UP);
        return e.getValue();
    }

    private BigDecimal normaliseHeight(BodyMetricEntity e) {
        if ("m".equalsIgnoreCase(e.getUnit()))
            return e.getValue().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
        return e.getValue();
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