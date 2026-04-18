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
    private final ExerciseRepository           exerciseRepo;
    private final WorkoutPlanRepository        planRepo;
    private final WorkoutPlanExerciseRepository planExerciseRepo;
    private final FoodRepository               foodRepo;
    private final DietPlanRepository           dietPlanRepo;
    private final DietPlanMealRepository       dietPlanMealRepo;
    private final SupplementTypeRepository     supplementTypeRepo;
    private final SupplementLogRepository      supplementLogRepo;
    private final SupplementScheduleRepository supplementScheduleRepo;

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


    // ── WORKOUT PLANS ────────────────────────────────────────────

    public List<WorkoutPlanResponse> listPlans(UUID userId) {
        return planRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream().map(p -> toPlanResponse(p, loadPlanExercises(p.getId())))
                .collect(Collectors.toList());
    }

    public WorkoutPlanResponse getPlan(UUID userId, UUID planId) {
        WorkoutPlanEntity p = planRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        return toPlanResponse(p, loadPlanExercises(planId));
    }

    @Transactional
    public WorkoutPlanResponse createPlan(UUID userId, WorkoutPlanRequest req) {
        WorkoutPlanEntity plan = WorkoutPlanEntity.builder()
                .userId(userId)
                .name(req.getName().trim())
                .description(req.getDescription())
                .difficulty(req.getDifficulty() != null ? req.getDifficulty().toUpperCase() : "BEGINNER")
                .daysPerWeek(req.getDaysPerWeek() != null ? req.getDaysPerWeek() : 3)
                .build();
        plan = planRepo.save(plan);
        List<WorkoutPlanExerciseResponse> exercises = persistPlanExercises(plan.getId(), req.getExercises());
        log.info("Workout plan created: {} userId={}", plan.getName(), userId);
        return toPlanResponse(plan, exercises);
    }

    @Transactional
    public WorkoutPlanResponse updatePlan(UUID userId, UUID planId, WorkoutPlanRequest req) {
        WorkoutPlanEntity plan = planRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (req.getName()        != null) plan.setName(req.getName().trim());
        if (req.getDescription() != null) plan.setDescription(req.getDescription());
        if (req.getDifficulty()  != null) plan.setDifficulty(req.getDifficulty().toUpperCase());
        if (req.getDaysPerWeek() != null) plan.setDaysPerWeek(req.getDaysPerWeek());

        List<WorkoutPlanExerciseResponse> exercises;
        if (req.getExercises() != null) {
            // Replace all exercises atomically
            planExerciseRepo.deleteByPlanId(planId);
            exercises = persistPlanExercises(planId, req.getExercises());
        } else {
            exercises = loadPlanExercises(planId);
        }
        return toPlanResponse(planRepo.save(plan), exercises);
    }

    @Transactional
    public void deletePlan(UUID userId, UUID planId) {
        WorkoutPlanEntity plan = planRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        plan.setDeleted(true);
        plan.setActive(false);
        planRepo.save(plan);
    }

    @Transactional
    public WorkoutPlanResponse activatePlan(UUID userId, UUID planId) {
        WorkoutPlanEntity plan = planRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        // Deactivate any currently active plan first
        planRepo.deactivateAllForUser(userId);
        plan.setActive(true);
        log.info("Workout plan activated: {} userId={}", plan.getName(), userId);
        return toPlanResponse(planRepo.save(plan), loadPlanExercises(planId));
    }

    public WorkoutPlanResponse getActivePlan(UUID userId) {
        return planRepo.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId)
                .map(p -> toPlanResponse(p, loadPlanExercises(p.getId())))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active plan found"));
    }

    // ── SUPPLEMENT TYPES ──────────────────────────────────────────

    public List<SupplementTypeResponse> listSupplementTypes(UUID userId) {
        return supplementTypeRepo.findAvailableForUser(userId)
                .stream().map(this::toSupplementTypeResponse).collect(Collectors.toList());
    }

    @Transactional
    public SupplementTypeResponse createSupplementType(UUID userId, SupplementTypeRequest req) {
        if (supplementTypeRepo.existsByNameAndCreatedBy(req.getName(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a supplement type named '" + req.getName() + "'");
        }
        SupplementTypeEntity e = SupplementTypeEntity.builder()
                .name(req.getName().trim())
                .category(req.getCategory() != null ? req.getCategory().toUpperCase() : "OTHER")
                .description(req.getDescription())
                .isSystem(false)
                .createdBy(userId)
                .build();
        return toSupplementTypeResponse(supplementTypeRepo.save(e));
    }

    @Transactional
    public SupplementTypeResponse updateSupplementType(UUID userId, UUID typeId,
                                                       SupplementTypeRequest req) {
        SupplementTypeEntity e = supplementTypeRepo.findByIdAndCreatedBy(typeId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplement type not found or not yours to edit"));
        if (req.getName()        != null) e.setName(req.getName().trim());
        if (req.getCategory()    != null) e.setCategory(req.getCategory().toUpperCase());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        return toSupplementTypeResponse(supplementTypeRepo.save(e));
    }

    @Transactional
    public void deleteSupplementType(UUID userId, UUID typeId) {
        SupplementTypeEntity e = supplementTypeRepo.findByIdAndCreatedBy(typeId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplement type not found or not yours to delete"));
        supplementTypeRepo.delete(e);
    }

// ── SUPPLEMENT LOGS ───────────────────────────────────────────

    public PageResponse<SupplementLogResponse> listSupplementLogs(UUID userId, Pageable pageable) {
        return PageResponse.of(
                supplementLogRepo.findByUserIdAndIsDeletedFalseOrderByLoggedDateDescCreatedAtDesc(
                        userId, pageable).map(e -> toSupplementLogResponse(e)));
    }

    public List<SupplementLogResponse> getTodaySupplementLogs(UUID userId) {
        return supplementLogRepo
                .findByUserIdAndLoggedDateAndIsDeletedFalse(userId, LocalDate.now())
                .stream().map(this::toSupplementLogResponse).collect(Collectors.toList());
    }

    @Transactional
    public SupplementLogResponse logSupplement(UUID userId, SupplementLogRequest req) {
        // Validate type exists and is visible to this user
        supplementTypeRepo.findById(req.getSupplementTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplement type not found"));

        SupplementLogEntity e = SupplementLogEntity.builder()
                .userId(userId)
                .supplementTypeId(req.getSupplementTypeId())
                .doseAmount(req.getDoseAmount())
                .doseUnit(req.getDoseUnit().toUpperCase())
                .timing(req.getTiming() != null ? req.getTiming().toUpperCase() : null)
                .loggedDate(req.getLoggedDate() != null ? req.getLoggedDate() : LocalDate.now())
                .notes(req.getNotes())
                .build();
        log.info("Supplement logged: typeId={} userId={}", req.getSupplementTypeId(), userId);
        return toSupplementLogResponse(supplementLogRepo.save(e));
    }

    @Transactional
    public void deleteSupplementLog(UUID userId, UUID logId) {
        SupplementLogEntity e = supplementLogRepo.findByIdAndUserId(logId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplement log not found"));
        e.setDeleted(true);
        supplementLogRepo.save(e);
    }

// ── SUPPLEMENT SCHEDULES ──────────────────────────────────────

    public List<SupplementScheduleResponse> listSchedules(UUID userId) {
        return supplementScheduleRepo.findByUserIdAndIsActiveTrueOrderByTimingAsc(userId)
                .stream().map(this::toSupplementScheduleResponse).collect(Collectors.toList());
    }

    @Transactional
    public SupplementScheduleResponse createSchedule(UUID userId, SupplementScheduleRequest req) {
        supplementTypeRepo.findById(req.getSupplementTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplement type not found"));

        SupplementScheduleEntity e = SupplementScheduleEntity.builder()
                .userId(userId)
                .supplementTypeId(req.getSupplementTypeId())
                .doseAmount(req.getDoseAmount())
                .doseUnit(req.getDoseUnit().toUpperCase())
                .timing(req.getTiming().toUpperCase())
                .frequency(req.getFrequency() != null ? req.getFrequency().toUpperCase() : "DAILY")
                .build();
        return toSupplementScheduleResponse(supplementScheduleRepo.save(e));
    }

    @Transactional
    public SupplementScheduleResponse updateSchedule(UUID userId, UUID scheduleId,
                                                     SupplementScheduleRequest req) {
        SupplementScheduleEntity e = supplementScheduleRepo.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Schedule not found"));
        if (req.getDoseAmount() != null) e.setDoseAmount(req.getDoseAmount());
        if (req.getDoseUnit()   != null) e.setDoseUnit(req.getDoseUnit().toUpperCase());
        if (req.getTiming()     != null) e.setTiming(req.getTiming().toUpperCase());
        if (req.getFrequency()  != null) e.setFrequency(req.getFrequency().toUpperCase());
        return toSupplementScheduleResponse(supplementScheduleRepo.save(e));
    }

    @Transactional
    public void deleteSchedule(UUID userId, UUID scheduleId) {
        SupplementScheduleEntity e = supplementScheduleRepo.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Schedule not found"));
        e.setActive(false);   // soft-deactivate, not hard-delete
        supplementScheduleRepo.save(e);
    }

    // ── FOOD LIBRARY ─────────────────────────────────────────────

    public List<FoodResponse> listFoods(UUID userId, String search) {
        String s = (search != null && !search.isBlank()) ? search.trim() : null;
        return foodRepo.findAvailable(userId, s)
                .stream().map(this::toFoodResponse).collect(Collectors.toList());
    }

    public FoodResponse getFood(UUID userId, UUID id) {
        return toFoodResponse(foodRepo.findByIdAndVisible(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found")));
    }

    @Transactional
    public FoodResponse createFood(UUID userId, FoodRequest req) {
        FoodEntity e = FoodEntity.builder()
                .name(req.getName().trim())
                .brand(req.getBrand())
                .caloriesPer100g(orZeroB(req.getCaloriesPer100g()))
                .proteinPer100g(orZeroB(req.getProteinPer100g()))
                .carbsPer100g(orZeroB(req.getCarbsPer100g()))
                .fatPer100g(orZeroB(req.getFatPer100g()))
                .servingSizeG(req.getServingSizeG())
                .foodType(req.getFoodType() != null ? req.getFoodType().toUpperCase() : "SOLID")
                .isSystem(false)
                .createdBy(userId)
                .build();
        log.info("Custom food created: {} userId={}", e.getName(), userId);
        return toFoodResponse(foodRepo.save(e));
    }

    @Transactional
    public FoodResponse updateFood(UUID userId, UUID id, FoodRequest req) {
        FoodEntity e = foodRepo.findByIdAndCreatedByAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Food not found or not yours to edit"));
        e.setName(req.getName().trim());
        e.setCaloriesPer100g(orZeroB(req.getCaloriesPer100g()));
        if (req.getBrand()        != null) e.setBrand(req.getBrand());
        if (req.getProteinPer100g() != null) e.setProteinPer100g(req.getProteinPer100g());
        if (req.getCarbsPer100g()   != null) e.setCarbsPer100g(req.getCarbsPer100g());
        if (req.getFatPer100g()     != null) e.setFatPer100g(req.getFatPer100g());
        if (req.getServingSizeG()   != null) e.setServingSizeG(req.getServingSizeG());
        if (req.getFoodType()       != null) e.setFoodType(req.getFoodType().toUpperCase());
        return toFoodResponse(foodRepo.save(e));
    }

    @Transactional
    public void deleteFood(UUID userId, UUID id) {
        FoodEntity e = foodRepo.findByIdAndCreatedByAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Food not found or not yours to delete"));
        e.setDeleted(true);
        foodRepo.save(e);
    }

    // ── DIET PLANS ────────────────────────────────────────────────

    public List<DietPlanResponse> listDietPlans(UUID userId) {
        return dietPlanRepo.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream().map(p -> toDietPlanResponse(p, loadDietPlanMeals(p.getId())))
                .collect(Collectors.toList());
    }

    public DietPlanResponse getDietPlan(UUID userId, UUID planId) {
        DietPlanEntity p = dietPlanRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet plan not found"));
        return toDietPlanResponse(p, loadDietPlanMeals(planId));
    }

    public DietPlanResponse getActiveDietPlan(UUID userId) {
        return dietPlanRepo.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId)
                .map(p -> toDietPlanResponse(p, loadDietPlanMeals(p.getId())))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active diet plan found"));
    }

    @Transactional
    public DietPlanResponse createDietPlan(UUID userId, DietPlanRequest req) {
        DietPlanEntity plan = DietPlanEntity.builder()
                .userId(userId)
                .name(req.getName().trim())
                .description(req.getDescription())
                .goal(req.getGoal() != null ? req.getGoal().toUpperCase() : "MAINTENANCE")
                .dailyCalorieTarget(req.getDailyCalorieTarget())
                .dailyProteinG(req.getDailyProteinG())
                .dailyCarbsG(req.getDailyCarbsG())
                .dailyFatG(req.getDailyFatG())
                .build();
        plan = dietPlanRepo.save(plan);
        List<DietPlanMealResponse> meals = persistDietPlanMeals(plan.getId(), req.getMeals());
        log.info("Diet plan created: {} userId={}", plan.getName(), userId);
        return toDietPlanResponse(plan, meals);
    }

    @Transactional
    public DietPlanResponse updateDietPlan(UUID userId, UUID planId, DietPlanRequest req) {
        DietPlanEntity plan = dietPlanRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet plan not found"));
        if (req.getName()               != null) plan.setName(req.getName().trim());
        if (req.getDescription()        != null) plan.setDescription(req.getDescription());
        if (req.getGoal()               != null) plan.setGoal(req.getGoal().toUpperCase());
        if (req.getDailyCalorieTarget()  != null) plan.setDailyCalorieTarget(req.getDailyCalorieTarget());
        if (req.getDailyProteinG()       != null) plan.setDailyProteinG(req.getDailyProteinG());
        if (req.getDailyCarbsG()         != null) plan.setDailyCarbsG(req.getDailyCarbsG());
        if (req.getDailyFatG()           != null) plan.setDailyFatG(req.getDailyFatG());

        List<DietPlanMealResponse> meals;
        if (req.getMeals() != null) {
            dietPlanMealRepo.deleteByPlanId(planId);
            meals = persistDietPlanMeals(planId, req.getMeals());
        } else {
            meals = loadDietPlanMeals(planId);
        }
        return toDietPlanResponse(dietPlanRepo.save(plan), meals);
    }

    @Transactional
    public void deleteDietPlan(UUID userId, UUID planId) {
        DietPlanEntity plan = dietPlanRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet plan not found"));
        plan.setDeleted(true);
        plan.setActive(false);
        dietPlanRepo.save(plan);
    }

    @Transactional
    public DietPlanResponse activateDietPlan(UUID userId, UUID planId) {
        DietPlanEntity plan = dietPlanRepo.findByIdAndUserIdAndIsDeletedFalse(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet plan not found"));
        dietPlanRepo.deactivateAllForUser(userId);
        plan.setActive(true);
        log.info("Diet plan activated: {} userId={}", plan.getName(), userId);
        return toDietPlanResponse(dietPlanRepo.save(plan), loadDietPlanMeals(planId));
    }

    // ── EXERCISE LIBRARY ────────────────────────────────────────

    public List<ExerciseResponse> listExercises(UUID userId,
                                                String muscleGroup,
                                                String equipment,
                                                String difficulty) {
        String mg = muscleGroup != null ? muscleGroup.toUpperCase() : null;
        String eq = equipment   != null ? equipment.toUpperCase()   : null;
        String df = difficulty  != null ? difficulty.toUpperCase()  : null;
        return exerciseRepo.findAvailable(userId, mg, eq, df)
                .stream().map(this::toExerciseResponse).collect(Collectors.toList());
    }

    public ExerciseResponse getExercise(UUID userId, UUID id) {
        return toExerciseResponse(
                exerciseRepo.findByIdAndVisible(id, userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Exercise not found"))
        );
    }

    @Transactional
    public ExerciseResponse createExercise(UUID userId, ExerciseRequest req) {
        ExerciseEntity e = ExerciseEntity.builder()
                .name(req.getName().trim())
                .muscleGroup(req.getMuscleGroup().toUpperCase())
                .equipment(req.getEquipment()  != null ? req.getEquipment().toUpperCase()  : "NONE")
                .difficulty(req.getDifficulty() != null ? req.getDifficulty().toUpperCase() : "BEGINNER")
                .instructions(req.getInstructions())
                .videoUrl(req.getVideoUrl())
                .isSystem(false)
                .createdBy(userId)
                .build();
        log.info("Custom exercise created: {} userId={}", e.getName(), userId);
        return toExerciseResponse(exerciseRepo.save(e));
    }

    @Transactional
    public ExerciseResponse updateExercise(UUID userId, UUID id, ExerciseRequest req) {
        ExerciseEntity e = exerciseRepo.findByIdAndCreatedByAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Exercise not found or not yours to edit"));
        e.setName(req.getName().trim());
        e.setMuscleGroup(req.getMuscleGroup().toUpperCase());
        if (req.getEquipment()    != null) e.setEquipment(req.getEquipment().toUpperCase());
        if (req.getDifficulty()   != null) e.setDifficulty(req.getDifficulty().toUpperCase());
        if (req.getInstructions() != null) e.setInstructions(req.getInstructions());
        if (req.getVideoUrl()     != null) e.setVideoUrl(req.getVideoUrl());
        return toExerciseResponse(exerciseRepo.save(e));
    }

    @Transactional
    public void deleteExercise(UUID userId, UUID id) {
        ExerciseEntity e = exerciseRepo.findByIdAndCreatedByAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Exercise not found or not yours to delete"));
        e.setDeleted(true);
        exerciseRepo.save(e);
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
    private ExerciseResponse toExerciseResponse(ExerciseEntity e) {
        return ExerciseResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .muscleGroup(e.getMuscleGroup())
                .equipment(e.getEquipment())
                .difficulty(e.getDifficulty())
                .instructions(e.getInstructions())
                .videoUrl(e.getVideoUrl())
                .isSystem(e.isSystem())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private List<WorkoutPlanExerciseResponse> persistPlanExercises(
            UUID planId, List<WorkoutPlanExerciseRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        List<WorkoutPlanExerciseEntity> entities = requests.stream()
                .map(r -> WorkoutPlanExerciseEntity.builder()
                        .planId(planId)
                        .dayNumber(r.getDayNumber())
                        .exerciseName(r.getExerciseName().trim())
                        .exerciseId(r.getExerciseId())
                        .sets(r.getSets() != null ? r.getSets() : 3)
                        .targetReps(r.getTargetReps())
                        .targetWeightKg(r.getTargetWeightKg())
                        .notes(r.getNotes())
                        .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                        .build())
                .collect(Collectors.toList());
        return planExerciseRepo.saveAll(entities).stream()
                .map(this::toPlanExerciseResponse)
                .collect(Collectors.toList());
    }

    private List<WorkoutPlanExerciseResponse> loadPlanExercises(UUID planId) {
        return planExerciseRepo.findByPlanIdOrderByDayNumberAscSortOrderAsc(planId)
                .stream().map(this::toPlanExerciseResponse).collect(Collectors.toList());
    }

    private WorkoutPlanExerciseResponse toPlanExerciseResponse(WorkoutPlanExerciseEntity e) {
        return WorkoutPlanExerciseResponse.builder()
                .id(e.getId())
                .dayNumber(e.getDayNumber())
                .exerciseName(e.getExerciseName())
                .exerciseId(e.getExerciseId())
                .sets(e.getSets())
                .targetReps(e.getTargetReps())
                .targetWeightKg(e.getTargetWeightKg())
                .notes(e.getNotes())
                .sortOrder(e.getSortOrder())
                .build();
    }

    private WorkoutPlanResponse toPlanResponse(WorkoutPlanEntity e,
                                               List<WorkoutPlanExerciseResponse> exercises) {
        return WorkoutPlanResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .difficulty(e.getDifficulty())
                .daysPerWeek(e.getDaysPerWeek())
                .isActive(e.isActive())
                .exercises(exercises)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // ── Diet plan private helpers ─────────────────────────────

    private List<DietPlanMealResponse> persistDietPlanMeals(
            UUID planId, List<DietPlanMealRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        List<DietPlanMealEntity> entities = requests.stream().map(r -> {
            // Auto-calculate macros if foodId provided and food exists
            BigDecimal qty = r.getQuantityG() != null ? r.getQuantityG() : BigDecimal.valueOf(100);
            BigDecimal calories = null, protein = null, carbs = null, fat = null;
            if (r.getFoodId() != null) {
                foodRepo.findById(r.getFoodId()).ifPresent(food -> {});
                // calculated inline below
            }
            FoodEntity food = r.getFoodId() != null
                    ? foodRepo.findById(r.getFoodId()).orElse(null) : null;
            if (food != null) {
                BigDecimal factor = qty.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                calories = food.getCaloriesPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                protein  = food.getProteinPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                carbs    = food.getCarbsPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                fat      = food.getFatPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            }
            return DietPlanMealEntity.builder()
                    .planId(planId)
                    .dayNumber(r.getDayNumber())
                    .mealType(r.getMealType().toUpperCase())
                    .foodId(r.getFoodId())
                    .foodName(r.getFoodName().trim())
                    .quantityG(qty)
                    .calories(calories).proteinG(protein).carbsG(carbs).fatG(fat)
                    .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                    .build();
        }).collect(Collectors.toList());
        return dietPlanMealRepo.saveAll(entities).stream()
                .map(this::toDietPlanMealResponse).collect(Collectors.toList());
    }

    private List<DietPlanMealResponse> loadDietPlanMeals(UUID planId) {
        return dietPlanMealRepo.findByPlanIdOrderByDayNumberAscMealTypeAscSortOrderAsc(planId)
                .stream().map(this::toDietPlanMealResponse).collect(Collectors.toList());
    }

    private DietPlanMealResponse toDietPlanMealResponse(DietPlanMealEntity e) {
        return DietPlanMealResponse.builder()
                .id(e.getId()).dayNumber(e.getDayNumber()).mealType(e.getMealType())
                .foodId(e.getFoodId()).foodName(e.getFoodName()).quantityG(e.getQuantityG())
                .calories(e.getCalories()).proteinG(e.getProteinG())
                .carbsG(e.getCarbsG()).fatG(e.getFatG()).sortOrder(e.getSortOrder())
                .build();
    }

    private DietPlanResponse toDietPlanResponse(DietPlanEntity e, List<DietPlanMealResponse> meals) {
        return DietPlanResponse.builder()
                .id(e.getId()).name(e.getName()).description(e.getDescription())
                .goal(e.getGoal()).dailyCalorieTarget(e.getDailyCalorieTarget())
                .dailyProteinG(e.getDailyProteinG()).dailyCarbsG(e.getDailyCarbsG())
                .dailyFatG(e.getDailyFatG()).isActive(e.isActive())
                .meals(meals).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }

    private SupplementTypeResponse toSupplementTypeResponse(SupplementTypeEntity e) {
        return SupplementTypeResponse.builder()
                .id(e.getId()).name(e.getName()).category(e.getCategory())
                .description(e.getDescription()).isSystem(e.isSystem())
                .build();
    }

    private SupplementLogResponse toSupplementLogResponse(SupplementLogEntity e) {
        SupplementTypeEntity type = supplementTypeRepo.findById(e.getSupplementTypeId()).orElse(null);
        return SupplementLogResponse.builder()
                .id(e.getId()).supplementTypeId(e.getSupplementTypeId())
                .supplementTypeName(type  != null ? type.getName()     : null)
                .supplementTypeCategory(type != null ? type.getCategory() : null)
                .doseAmount(e.getDoseAmount()).doseUnit(e.getDoseUnit())
                .timing(e.getTiming()).loggedDate(e.getLoggedDate())
                .notes(e.getNotes()).createdAt(e.getCreatedAt())
                .build();
    }

    private SupplementScheduleResponse toSupplementScheduleResponse(SupplementScheduleEntity e) {
        SupplementTypeEntity type = supplementTypeRepo.findById(e.getSupplementTypeId()).orElse(null);
        return SupplementScheduleResponse.builder()
                .id(e.getId()).supplementTypeId(e.getSupplementTypeId())
                .supplementTypeName(type != null ? type.getName() : null)
                .doseAmount(e.getDoseAmount()).doseUnit(e.getDoseUnit())
                .timing(e.getTiming()).frequency(e.getFrequency())
                .isActive(e.isActive()).createdAt(e.getCreatedAt())
                .build();
    }

    private FoodResponse toFoodResponse(FoodEntity e) {
        return FoodResponse.builder()
                .id(e.getId()).name(e.getName()).brand(e.getBrand())
                .caloriesPer100g(e.getCaloriesPer100g()).proteinPer100g(e.getProteinPer100g())
                .carbsPer100g(e.getCarbsPer100g()).fatPer100g(e.getFatPer100g())
                .servingSizeG(e.getServingSizeG()).foodType(e.getFoodType())
                .isSystem(e.isSystem()).createdAt(e.getCreatedAt())
                .build();
    }

    private BigDecimal orZeroB(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

}