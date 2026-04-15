package com.corecompass.habits.controller;
import com.corecompass.habits.dto.*;
import com.corecompass.habits.service.HabitsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequiredArgsConstructor
public class HabitsController {
    private final HabitsService svc;

    // ── CATEGORY TYPES ────────────────────────────────────
    @GetMapping("/api/v1/habits/category-types")
    public ResponseEntity<ApiResponse<List<HabitCategoryTypeDTO>>> listCategoryTypes(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listCategoryTypes(uid), null));
    }

    // ── HABITS CRUD ───────────────────────────────────────
    @GetMapping("/api/v1/habits")
    public ResponseEntity<ApiResponse<List<HabitResponse>>> listHabits(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listHabitsWithTodayStatus(uid), null));
    }

    @PostMapping("/api/v1/habits")
    public ResponseEntity<ApiResponse<HabitResponse>> createHabit(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody HabitRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.createHabit(uid, req), "Habit created"));
    }

    @GetMapping("/api/v1/habits/dashboard")
    public ResponseEntity<ApiResponse<List<HabitResponse>>> dashboard(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listHabitsWithTodayStatus(uid), null));
    }

    @GetMapping("/api/v1/habits/{habitId}")
    public ResponseEntity<ApiResponse<HabitResponse>> getHabit(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getHabit(uid, habitId), null));
    }

    @PutMapping("/api/v1/habits/{habitId}")
    public ResponseEntity<ApiResponse<HabitResponse>> updateHabit(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId, @Valid @RequestBody HabitRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateHabit(uid, habitId, req), "Updated"));
    }

    @DeleteMapping("/api/v1/habits/{habitId}")
    public ResponseEntity<ApiResponse<Void>> deleteHabit(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId) {
        svc.deleteHabit(uid, habitId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    @PatchMapping("/api/v1/habits/{habitId}/pause")
    public ResponseEntity<ApiResponse<HabitResponse>> pause(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.pauseHabit(uid, habitId), "Paused"));
    }

    @PatchMapping("/api/v1/habits/{habitId}/resume")
    public ResponseEntity<ApiResponse<HabitResponse>> resume(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.resumeHabit(uid, habitId), "Resumed"));
    }

    // ── CHECK-INS ─────────────────────────────────────────
    @PostMapping("/api/v1/habits/{habitId}/check-in")
    public ResponseEntity<ApiResponse<CheckinResponse>> checkin(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId,
            @Valid @RequestBody CheckinRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.checkin(uid, habitId, req), "Checked in"));
    }

    @GetMapping("/api/v1/habits/{habitId}/check-ins")
    public ResponseEntity<ApiResponse<Page<CheckinResponse>>> checkinHistory(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="30") int size) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getCheckinHistory(uid, habitId, PageRequest.of(page, size)), null));
    }

    @PostMapping("/api/v1/habits/{habitId}/skip")
    public ResponseEntity<ApiResponse<CheckinResponse>> skip(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId,
            @RequestBody CheckinRequest req) {
        req.setSkip(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.checkin(uid, habitId, req), "Skipped"));
    }

    // ── STREAKS ───────────────────────────────────────────
    @GetMapping("/api/v1/habits/{habitId}/streaks")
    public ResponseEntity<ApiResponse<StreakResponse>> getStreaks(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID habitId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getStreaks(uid, habitId), null));
    }

    // ── HABIT STACKS ──────────────────────────────────────
    @GetMapping("/api/v1/habit-stacks")
    public ResponseEntity<ApiResponse<List<HabitStackResponse>>> listStacks(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listStacks(uid), null));
    }

    @PostMapping("/api/v1/habit-stacks")
    public ResponseEntity<ApiResponse<HabitStackResponse>> createStack(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody HabitStackRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.createStack(uid, req), "Stack created"));
    }

    @PutMapping("/api/v1/habit-stacks/{stackId}")
    public ResponseEntity<ApiResponse<HabitStackResponse>> updateStack(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID stackId,
            @Valid @RequestBody HabitStackRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateStack(uid, stackId, req), "Updated"));
    }

    @DeleteMapping("/api/v1/habit-stacks/{stackId}")
    public ResponseEntity<ApiResponse<Void>> deleteStack(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID stackId) {
        svc.deleteStack(uid, stackId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    // ── ROUTINES ──────────────────────────────────────────
    @GetMapping("/api/v1/routines")
    public ResponseEntity<ApiResponse<List<RoutineGroupResponse>>> listRoutines(@RequestHeader("X-User-Id") UUID uid) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listRoutines(uid), null));
    }

    @PostMapping("/api/v1/routines")
    public ResponseEntity<ApiResponse<RoutineGroupResponse>> createRoutine(
            @RequestHeader("X-User-Id") UUID uid, @Valid @RequestBody RoutineGroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(svc.createRoutine(uid, req), "Routine created"));
    }

    @PutMapping("/api/v1/routines/{routineId}")
    public ResponseEntity<ApiResponse<RoutineGroupResponse>> updateRoutine(
            @RequestHeader("X-User-Id") UUID uid, @PathVariable UUID routineId,
            @Valid @RequestBody RoutineGroupRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateRoutine(uid, routineId, req), "Updated"));
    }

    // ── INTERNAL FEIGN ────────────────────────────────────
    @GetMapping("/internal/habits/score")
    public ResponseEntity<Integer> internalScore(@RequestParam UUID userId) {
        return ResponseEntity.ok(svc.getHabitScore(userId));
    }
}
