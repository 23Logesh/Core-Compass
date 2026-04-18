package com.corecompass.core.controller;

import com.corecompass.core.dto.*;
import com.corecompass.core.service.DashboardService;
import com.corecompass.core.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ═══════════════════════════════════════════════════════════════
// GOAL TYPE CONTROLLER — /api/v1/goals/types
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/v1/goals/types")
@RequiredArgsConstructor
class GoalTypeController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalTypeDTO>>> listTypes(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.listGoalTypes(userId), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalTypeDTO>> createType(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody GoalTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(goalService.createGoalType(userId, request),
                "Goal type created successfully"));
    }

    @PutMapping("/{typeId}")
    public ResponseEntity<ApiResponse<GoalTypeDTO>> updateType(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID typeId,
            @Valid @RequestBody GoalTypeRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(goalService.updateGoalType(userId, typeId, request), "Updated"));
    }

    @DeleteMapping("/{typeId}")
    public ResponseEntity<ApiResponse<Void>> deleteType(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID typeId) {
        goalService.deleteGoalType(userId, typeId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Goal type deleted"));
    }
}

// ═══════════════════════════════════════════════════════════════
// GOAL CONTROLLER — /api/v1/goals
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GoalResponse>>> listGoals(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
            ApiResponse.ok(goalService.listGoals(userId, status, pageable), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(goalService.createGoal(userId, request),
                "Goal created successfully"));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getGoal(userId, goalId), null));
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.updateGoal(userId, goalId, request), "Goal updated"));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId) {
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Goal deleted"));
    }

    @PatchMapping("/{goalId}/archive")
    public ResponseEntity<ApiResponse<GoalResponse>> archiveGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.archiveGoal(userId, goalId), "Goal archived"));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapEntry>>> getHeatmap(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getHeatmap(userId), null));
    }
}

// ═══════════════════════════════════════════════════════════════
// TODO CONTROLLER — /api/v1/goals/{goalId}/todos & /api/v1/todos/today
// ═══════════════════════════════════════════════════════════════
@RestController
@RequiredArgsConstructor
class TodoController {

    private final GoalService goalService;

    @GetMapping("/api/v1/goals/{goalId}/todos")
    public ResponseEntity<ApiResponse<PageResponse<TodoResponse>>> listTodos(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.listTodos(userId, goalId, status,
                PageRequest.of(page, size)), null));
    }

    @PostMapping("/api/v1/goals/{goalId}/todos")
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(
                goalService.createTodo(userId, goalId, request),
                "Todo created successfully"));
    }

    @PutMapping("/api/v1/goals/{goalId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID todoId,
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.updateTodo(userId, goalId, todoId, request), "Todo updated"));
    }

    @PatchMapping("/api/v1/goals/{goalId}/todos/{todoId}/done")
    public ResponseEntity<ApiResponse<TodoResponse>> toggleDone(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID todoId) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.toggleTodoDone(userId, goalId, todoId), "Todo status toggled"));
    }

    @DeleteMapping("/api/v1/goals/{goalId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID todoId) {
        goalService.deleteTodo(userId, goalId, todoId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Todo deleted"));
    }

    @GetMapping("/api/v1/todos/today")
    public ResponseEntity<ApiResponse<List<TodoResponse>>> getTodaysTodos(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(
            goalService.getTodaysTodos(userId), null));
    }
}

// ADD this entire new controller class:
// ═══════════════════════════════════════════════════════════════
// MILESTONE CONTROLLER — /api/v1/goals/{goalId}/milestones
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/v1/goals/{goalId}/milestones")
@RequiredArgsConstructor
class MilestoneController {

    private final GoalService goalService;

    @PatchMapping("/{milestoneId}/toggle")
    public ResponseEntity<ApiResponse<MilestoneResponse>> toggleMilestone(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID goalId,
            @PathVariable UUID milestoneId) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.toggleMilestone(userId, goalId, milestoneId),
                "Milestone status toggled"));
    }
}

// ═══════════════════════════════════════════════════════════════
// DASHBOARD CONTROLLER — /api/v1/dashboard
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(
            dashboardService.getDashboard(userId), null));
    }
}

// ═══════════════════════════════════════════════════════════════
// INTERNAL CONTROLLER — /internal/core (called by Report Service via Feign)
// Not routed through gateway — service-to-service only
// ═══════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/internal/core")
@RequiredArgsConstructor
class InternalCoreController {

    private final GoalService goalService;

    @GetMapping("/active-users")
    public ResponseEntity<List<UUID>> getActiveUserIds() {
        return ResponseEntity.ok(goalService.getActiveUserIds());
    }

    @GetMapping("/goal-progress")
    public ResponseEntity<GoalProgressSummaryDTO> getGoalProgress(
            @RequestParam UUID userId) {
        return ResponseEntity.ok(goalService.getGoalProgressSummary(userId));
    }

}

    // ═══════════════════════════════════════════════════════════════
// ACTIVITY TYPE CONTROLLER — /api/v1/activity-types
// ═══════════════════════════════════════════════════════════════
    @RestController
    @RequestMapping("/api/v1/activity-types")
    @RequiredArgsConstructor
    class ActivityTypeController {

        private final GoalService goalService;

        @GetMapping
        public ResponseEntity<ApiResponse<List<ActivityTypeDTO>>> listTypes(
                @RequestHeader("X-User-Id") UUID userId) {
            return ResponseEntity.ok(ApiResponse.ok(goalService.listActivityTypes(userId), null));
        }

        @PostMapping
        public ResponseEntity<ApiResponse<ActivityTypeDTO>> createType(
                @RequestHeader("X-User-Id") UUID userId,
                @Valid @RequestBody ActivityTypeRequest request) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(goalService.createActivityType(userId, request),
                            "Activity type created"));
        }

        @PutMapping("/{typeId}")
        public ResponseEntity<ApiResponse<ActivityTypeDTO>> updateType(
                @RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID typeId,
                @Valid @RequestBody ActivityTypeRequest request) {
            return ResponseEntity.ok(ApiResponse.ok(
                    goalService.updateActivityType(userId, typeId, request), "Updated"));
        }

        @DeleteMapping("/{typeId}")
        public ResponseEntity<ApiResponse<Void>> deleteType(
                @RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID typeId) {
            goalService.deleteActivityType(userId, typeId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Activity type deleted"));
        }
    }

// ═══════════════════════════════════════════════════════════════
// ACTIVITY CONTROLLER — /api/v1/goals/{goalId}/activities
// ═══════════════════════════════════════════════════════════════
    @RestController
    @RequestMapping("/api/v1/goals/{goalId}/activities")
    @RequiredArgsConstructor
    class ActivityController {

        private final GoalService goalService;

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<ActivityResponse>>> listActivities(
                @RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID goalId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size) {
            return ResponseEntity.ok(ApiResponse.ok(
                    goalService.listActivities(userId, goalId,
                            PageRequest.of(page, size, Sort.by("loggedAt").descending())), null));
        }

        @PostMapping
        public ResponseEntity<ApiResponse<ActivityResponse>> logActivity(
                @RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID goalId,
                @Valid @RequestBody ActivityRequest request) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(
                            goalService.logActivity(userId, goalId, request),
                            "Activity logged"));
        }

        @DeleteMapping("/{activityId}")
        public ResponseEntity<ApiResponse<Void>> deleteActivity(
                @RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID goalId,
                @PathVariable UUID activityId) {
            goalService.deleteActivity(userId, goalId, activityId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Activity deleted"));
        }
    }


