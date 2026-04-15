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

    // ── SLEEP ───────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/sleep")
    public ResponseEntity<ApiResponse<SleepResponse>> logSleep(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody SleepRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logSleep(uid, req), "Sleep logged"));
    }

    // ── HYDRATION ───────────────────────────────────────────
    @PostMapping("/api/v1/fitness/hydration")
    public ResponseEntity<ApiResponse<HydrationResponse>> logHydration(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody HydrationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logHydration(uid, req), "Hydration logged"));
    }

    // ── MOOD ────────────────────────────────────────────────
    @PostMapping("/api/v1/fitness/mood")
    public ResponseEntity<ApiResponse<MoodResponse>> logMood(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody MoodRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(svc.logMood(uid, req), "Mood logged"));
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

    // ── INTERNAL FEIGN (called by Report + Core services) ───
    @GetMapping("/internal/fitness/summary/weekly")
    public ResponseEntity<FitnessSummaryDTO> internalSummary(
            @RequestParam UUID userId,
            @RequestParam String weekStart) {
        return ResponseEntity.ok(svc.getWeeklySummaryForFeign(userId, weekStart));
    }
}
