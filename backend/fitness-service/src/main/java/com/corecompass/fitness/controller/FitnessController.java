package com.corecompass.fitness.controller;
import com.corecompass.fitness.dto.*;
import com.corecompass.fitness.service.FitnessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class FitnessController {

    private final FitnessService svc;

    // ── CARDIO ──────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/cardio")
    public ResponseEntity<ApiResponse<CardioResponse>> logCardio(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody CardioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logCardio(uid, req), "Cardio logged"));
    }

    @GetMapping("/api/v1/fitness/cardio")
    public ResponseEntity<ApiResponse<PageResponse<CardioResponse>>> listCardio(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(required=false) String type,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            svc.listCardio(uid, type, PageRequest.of(page, size, Sort.by("loggedDate").descending())), null));
    }

    @DeleteMapping("/api/v1/fitness/cardio/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCardio(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id) {
        svc.deleteCardio(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    @PutMapping("/api/v1/fitness/cardio/{id}")
    public ResponseEntity<ApiResponse<CardioResponse>> updateCardio(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody CardioUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateCardio(uid, id, req), "Cardio updated"));
    }

    // ── WORKOUTS ────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/workouts")
    public ResponseEntity<ApiResponse<WorkoutResponse>> logWorkout(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody WorkoutRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logWorkout(uid, req), "Workout logged"));
    }

    @GetMapping("/api/v1/fitness/workouts")
    public ResponseEntity<ApiResponse<PageResponse<WorkoutResponse>>> listWorkouts(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            svc.listWorkouts(uid, PageRequest.of(page, size, Sort.by("sessionDate").descending())), null));
    }

    @DeleteMapping("/api/v1/fitness/workouts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkout(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id) {
        svc.deleteWorkout(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    @PutMapping("/api/v1/fitness/workouts/{id}")
    public ResponseEntity<ApiResponse<WorkoutResponse>> updateWorkout(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody WorkoutUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateWorkout(uid, id, req), "Workout updated"));
    }

    @GetMapping("/api/v1/fitness/workouts/prs")
    public ResponseEntity<ApiResponse<List<WorkoutPRResponse>>> getWorkoutPRs(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getWorkoutPRs(uid), null));
    }

    // ── MEALS ───────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/meals")
    public ResponseEntity<ApiResponse<MealResponse>> logMeal(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody MealRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logMeal(uid, req), "Meal logged"));
    }

    @GetMapping("/api/v1/fitness/meals/today")
    public ResponseEntity<ApiResponse<DailyMacroSummary>> getMealsToday(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getMealsForDay(uid, LocalDate.now()), null));
    }

    @GetMapping("/api/v1/fitness/meals")
    public ResponseEntity<ApiResponse<PageResponse<MealResponse>>> listMeals(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            svc.listMeals(uid, PageRequest.of(page, size)), null));
    }

    @PutMapping("/api/v1/fitness/meals/{id}")
    public ResponseEntity<ApiResponse<MealResponse>> updateMeal(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody MealUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateMeal(uid, id, req), "Meal updated"));
    }

    // ── BODY METRICS ────────────────────────────────────────
    @PostMapping("/api/v1/fitness/metrics")
    public ResponseEntity<ApiResponse<List<BodyMetricResponse>>> logMetrics(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody List<BodyMetricRequest> req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logBodyMetrics(uid, req), "Metrics logged"));
    }

    @GetMapping("/api/v1/fitness/metrics")
    public ResponseEntity<ApiResponse<PageResponse<BodyMetricResponse>>> listMetrics(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(required=false) String type,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            svc.listMetrics(uid, type, PageRequest.of(page, size)), null));
    }

    @PutMapping("/api/v1/fitness/metrics/{id}")
    public ResponseEntity<ApiResponse<BodyMetricResponse>> updateMetric(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody BodyMetricUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateMetric(uid, id, req), "Metric updated"));
    }

    @GetMapping("/api/v1/fitness/metrics/stats")
    public ResponseEntity<ApiResponse<BodyMetricStatsResponse>> getMetricStats(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getMetricStats(uid), null));
    }

    @GetMapping("/api/v1/fitness/metrics/trends")
    public ResponseEntity<ApiResponse<MetricTrendsResponse>> getMetricTrends(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam String type,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getMetricTrends(uid, type, days), null));
    }

    // ── SLEEP ───────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/sleep")
    public ResponseEntity<ApiResponse<SleepResponse>> logSleep(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody SleepRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logSleep(uid, req), "Sleep logged"));
    }

    @GetMapping("/api/v1/fitness/sleep")
    public ResponseEntity<ApiResponse<PageResponse<SleepResponse>>> listSleep(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "14") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listSleep(uid, PageRequest.of(page, size)), null));
    }

    @PutMapping("/api/v1/fitness/sleep/{id}")
    public ResponseEntity<ApiResponse<SleepResponse>> updateSleep(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody SleepUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateSleep(uid, id, req), "Sleep updated"));
    }

    // ── HYDRATION ───────────────────────────────────────────
    @PostMapping("/api/v1/fitness/hydration")
    public ResponseEntity<ApiResponse<HydrationResponse>> logHydration(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody HydrationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logHydration(uid, req), "Hydration logged"));
    }

    @GetMapping("/api/v1/fitness/hydration/history")
    public ResponseEntity<ApiResponse<PageResponse<HydrationResponse>>> listHydration(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "14") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listHydration(uid, PageRequest.of(page, size)), null));
    }

    @PutMapping("/api/v1/fitness/hydration/{id}")
    public ResponseEntity<ApiResponse<HydrationResponse>> updateHydration(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody HydrationUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateHydration(uid, id, req), "Hydration updated"));
    }

    // ── MOOD ────────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/mood")
    public ResponseEntity<ApiResponse<MoodResponse>> logMood(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody MoodRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logMood(uid, req), "Mood logged"));
    }

    @GetMapping("/api/v1/fitness/mood")
    public ResponseEntity<ApiResponse<PageResponse<MoodResponse>>> listMood(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "14") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listMood(uid, PageRequest.of(page, size)), null));
    }

    @PutMapping("/api/v1/fitness/mood/{id}")
    public ResponseEntity<ApiResponse<MoodResponse>> updateMood(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID id,
            @Valid @RequestBody MoodUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateMood(uid, id, req), "Mood updated"));
    }

    // ── STREAKS ─────────────────────────────────────────────
    @GetMapping("/api/v1/fitness/streaks")
    public ResponseEntity<ApiResponse<List<StreakResponse>>> getStreaks(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getStreaks(uid), null));
    }

    // ── WEEKLY SUMMARY ──────────────────────────────────────
    @GetMapping("/api/v1/fitness/summary/weekly")
    public ResponseEntity<ApiResponse<FitnessSummaryDTO>> weeklySummary(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam String weekStart) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getWeeklySummary(uid, weekStart), null));
    }

    @GetMapping("/api/v1/fitness/summary/monthly")
    public ResponseEntity<ApiResponse<MonthlySummaryDTO>> monthlySummary(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getMonthlySummary(uid, month), null));
    }

    @GetMapping("/api/v1/fitness/targets")
    public ResponseEntity<ApiResponse<FitnessTargetResponse>> getTargets(
            @RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getTargets(uid), null));
    }

    @PutMapping("/api/v1/fitness/targets")
    public ResponseEntity<ApiResponse<FitnessTargetResponse>> upsertTargets(
            @RequestHeader("X-User-Id") UUID uid,
            @Valid @RequestBody FitnessTargetRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.upsertTargets(uid, req), "Targets updated"));
    }

    // ── INTERNAL FEIGN (called by Report + Core services) ───
    @GetMapping("/internal/fitness/summary/weekly")
    public ResponseEntity<FitnessSummaryDTO> internalSummary(
            @RequestParam UUID userId,
            @RequestParam String weekStart) {
        return ResponseEntity.ok(svc.getWeeklySummaryForFeign(userId, weekStart));
    }

    // ── EXERCISE LIBRARY ─────────────────────────────────────────────────

    @GetMapping("/api/v1/fitness/exercises")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> listExercises(
            @RequestHeader("X-User-Id") UUID uid,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String difficulty) {
        return ResponseEntity.ok(ApiResponse.ok(
                svc.listExercises(uid, muscleGroup, equipment, difficulty), null));
    }

    @GetMapping("/api/v1/fitness/exercises/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> getExercise(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getExercise(uid, id), null));
    }

    @PostMapping("/api/v1/fitness/exercises")
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(
            @RequestHeader("X-User-Id") UUID uid,
            @Valid @RequestBody ExerciseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(svc.createExercise(uid, req), "Exercise created"));
    }

    @PutMapping("/api/v1/fitness/exercises/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id,
            @Valid @RequestBody ExerciseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateExercise(uid, id, req), "Updated"));
    }

    @DeleteMapping("/api/v1/fitness/exercises/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(
            @RequestHeader("X-User-Id") UUID uid,
            @PathVariable UUID id) {
        svc.deleteExercise(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }
}
