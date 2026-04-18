package com.corecompass.core.service;

import com.corecompass.core.dto.*;
import com.corecompass.core.entity.*;
import com.corecompass.core.exception.*;
import com.corecompass.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.corecompass.core.exception.AccessDeniedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository      goalRepository;
    private final GoalTypeRepository  goalTypeRepository;
    private final TodoRepository      todoRepository;
    private final MilestoneRepository milestoneRepository;
    private final GoogleCalendarService calendarService;
    private final ActivityRepository     activityRepository;
    private final ActivityTypeRepository activityTypeRepository;

    // ─────────────────────────────────────────────────────────
    // GOAL TYPES (Type Registry)
    // ─────────────────────────────────────────────────────────

    public List<GoalTypeDTO> listGoalTypes(UUID userId) {
        return goalTypeRepository.findAvailableForUser(userId)
            .stream()
            .map(this::toGoalTypeDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public GoalTypeDTO createGoalType(UUID userId, GoalTypeRequest request) {
        if (goalTypeRepository.existsByNameAndCreatedBy(request.getName(), userId)) {
            throw new DuplicateResourceException("DUPLICATE_TYPE",
                "You already have a goal type named '" + request.getName() + "'");
        }
        GoalTypeEntity entity = GoalTypeEntity.builder()
            .name(request.getName().trim())
            .icon(request.getIcon())
            .color(request.getColor())
            .unit(request.getUnit())
            .isPublic(request.isPublic())
            .createdBy(userId)
            .build();
        return toGoalTypeDTO(goalTypeRepository.save(entity));
    }

    @Transactional
    public GoalTypeDTO updateGoalType(UUID userId, UUID typeId, GoalTypeRequest request) {
        GoalTypeEntity entity = goalTypeRepository.findByIdAndCreatedBy(typeId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal type not found or not owned by you"));
        entity.setName(request.getName().trim());
        if (request.getIcon()  != null) entity.setIcon(request.getIcon());
        if (request.getColor() != null) entity.setColor(request.getColor());
        if (request.getUnit()  != null) entity.setUnit(request.getUnit());
        entity.setPublic(request.isPublic());
        return toGoalTypeDTO(goalTypeRepository.save(entity));
    }

    @Transactional
    public void deleteGoalType(UUID userId, UUID typeId) {
        GoalTypeEntity entity = goalTypeRepository.findByIdAndCreatedBy(typeId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal type not found or not owned by you"));
        if (entity.isSystem()) {
            throw new InvalidRequestException("CANNOT_DELETE_SYSTEM_TYPE", "System types cannot be deleted");
        }
        goalTypeRepository.delete(entity);
    }

    // ─────────────────────────────────────────────────────────
    // GOALS
    // ─────────────────────────────────────────────────────────

    public PageResponse<GoalResponse> listGoals(UUID userId, String status, Pageable pageable) {
        Page<GoalEntity> page = goalRepository.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.of(page.map(this::toGoalResponse));
    }

    @Transactional
    public GoalResponse createGoal(UUID userId, GoalRequest request) {
        // Validate category type exists and user can use it
        goalTypeRepository.findById(request.getCategoryTypeId())
            .orElseThrow(() -> new InvalidRequestException("INVALID_CATEGORY",
                "Goal category type not found"));

        GoalEntity goal = GoalEntity.builder()
            .userId(userId)
            .title(request.getTitle().trim())
            .categoryTypeId(request.getCategoryTypeId())
            .description(request.getDescription())
            .targetDate(request.getTargetDate())
            .color(request.getColor())
            .icon(request.getIcon())
            .createdBy(userId)
            .build();

        goal = goalRepository.save(goal);

        // Create milestones atomically — if any fail, goal is rolled back
        if (request.getMilestones() != null && !request.getMilestones().isEmpty()) {
            final UUID goalId = goal.getId();
            for (MilestoneRequest m : request.getMilestones()) {
                MilestoneEntity milestone = MilestoneEntity.builder()
                    .goalId(goalId)
                    .userId(userId)
                    .title(m.getTitle().trim())
                    .targetDate(m.getTargetDate())
                    .build();
                milestoneRepository.save(milestone);
            }
        }

        log.info("Goal created: id={} userId={}", goal.getId(), userId);
        return toGoalResponse(goal);
    }

    public GoalResponse getGoal(UUID userId, UUID goalId) {
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));
        return toGoalResponse(goal);
    }

    @Transactional
    public GoalResponse updateGoal(UUID userId, UUID goalId, GoalRequest request) {
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));

        goal.setTitle(request.getTitle().trim());
        goal.setCategoryTypeId(request.getCategoryTypeId());
        goal.setTargetDate(request.getTargetDate());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getColor()       != null) goal.setColor(request.getColor());
        if (request.getIcon()        != null) goal.setIcon(request.getIcon());

        return toGoalResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));
        goal.setDeleted(true);
        goalRepository.save(goal);
        log.info("Goal soft-deleted: id={} userId={}", goalId, userId);
    }

    @Transactional
    public GoalResponse archiveGoal(UUID userId, UUID goalId) {
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));
        goal.setStatus("ARCHIVED");
        return toGoalResponse(goalRepository.save(goal));
    }

    @Transactional
    public MilestoneResponse toggleMilestone(UUID userId, UUID goalId, UUID milestoneId) {
        validateGoalOwnership(userId, goalId);
        MilestoneEntity milestone = milestoneRepository.findByIdAndUserId(milestoneId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Milestone not found: " + milestoneId));

        milestone.setCompleted(!milestone.isCompleted());
        milestone.setCompletedAt(milestone.isCompleted() ? java.time.Instant.now() : null);
        return toMilestoneResponse(milestoneRepository.save(milestone));
    }

    // ─────────────────────────────────────────────────────────
    // TODOS
    // ─────────────────────────────────────────────────────────

    public PageResponse<TodoResponse> listTodos(UUID userId, UUID goalId,
                                                 String status, Pageable pageable) {
        // Verify goal ownership
        goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));

        Page<TodoEntity> page = todoRepository.findByGoalIdAndUserId(goalId, userId, status, pageable);
        return PageResponse.of(page.map(this::toTodoResponse));
    }

    @Transactional
    public TodoResponse createTodo(UUID userId, UUID goalId, TodoRequest request) {
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));

        // Duplicate check
        if (request.getDueDate() != null &&
            todoRepository.existsDuplicate(goalId, userId, request.getTitle(), request.getDueDate())) {
            throw new DuplicateResourceException("DUPLICATE_TODO",
                "A todo with this title already exists for this date and goal");
        }

        TodoEntity todo = TodoEntity.builder()
            .userId(userId)
            .goalId(goalId)
            .title(request.getTitle().trim())
            .description(request.getDescription())
            .dueDate(request.getDueDate())
            .dueTime(request.getDueTime() != null
                ? java.time.LocalTime.parse(request.getDueTime()) : null)
            .recurrenceRule(request.getRecurrenceRule())
            .createdBy(userId)
            .build();

        todo = todoRepository.save(todo);
        final TodoEntity savedTodo = todo;

        // Async Google Calendar sync (non-blocking — user gets instant 201)
        if (todo.getDueDate() != null && todo.getDueTime() != null) {
            calendarService.syncToCalendar(savedTodo, goal.getTitle());
        }

        log.info("Todo created: id={} goalId={} userId={}", todo.getId(), goalId, userId);
        return toTodoResponse(todo);
    }

    @Transactional
    public TodoResponse updateTodo(UUID userId, UUID goalId, UUID todoId, TodoRequest request) {
        validateGoalOwnership(userId, goalId);
        TodoEntity todo = todoRepository.findByIdAndUserId(todoId, userId)
            .orElseThrow(() -> new TodoNotFoundException("Todo not found: " + todoId));

        todo.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getDueDate()     != null) todo.setDueDate(request.getDueDate());
        if (request.getDueTime()     != null) todo.setDueTime(java.time.LocalTime.parse(request.getDueTime()));
        if (request.getRecurrenceRule() != null) todo.setRecurrenceRule(request.getRecurrenceRule());

        return toTodoResponse(todoRepository.save(todo));
    }

    /**
     * Toggle todo completion.
     * Atomically updates todo.completed + recalculates goal.progressPct.
     */
    @Transactional
    public TodoResponse toggleTodoDone(UUID userId, UUID goalId, UUID todoId) {
        validateGoalOwnership(userId, goalId);
        TodoEntity todo = todoRepository.findByIdAndUserId(todoId, userId)
            .orElseThrow(() -> new TodoNotFoundException("Todo not found: " + todoId));

        todo.setCompleted(!todo.isCompleted());
        todo.setCompletedAt(todo.isCompleted() ? java.time.Instant.now() : null);
        todoRepository.save(todo);

        // Recalculate goal progress atomically
        recalculateGoalProgress(goalId);

        log.info("Todo toggled: id={} completed={}", todoId, todo.isCompleted());
        return toTodoResponse(todo);
    }

    @Transactional
    public void deleteTodo(UUID userId, UUID goalId, UUID todoId) {
        validateGoalOwnership(userId, goalId);
        TodoEntity todo = todoRepository.findByIdAndUserId(todoId, userId)
            .orElseThrow(() -> new TodoNotFoundException("Todo not found: " + todoId));
        todo.setDeleted(true);
        todoRepository.save(todo);
        recalculateGoalProgress(goalId);
    }

    public List<TodoResponse> getTodaysTodos(UUID userId) {
        return todoRepository.findTodaysTodos(userId, LocalDate.now())
            .stream().map(this::toTodoResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // HEATMAP (90-day activity)
    // ─────────────────────────────────────────────────────────

    public List<HeatmapEntry> getHeatmap(UUID userId) {
        return goalRepository.findHeatmapData(userId).stream()
            .map(row -> HeatmapEntry.builder()
                .date(((java.sql.Date) row[0]).toLocalDate())
                .count(((Number) row[1]).intValue())
                .build()
            ).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // INTERNAL — used by Report Service via Feign
    // ─────────────────────────────────────────────────────────

    public List<UUID> getActiveUserIds() {
        return goalRepository.findActiveUserIds();
    }

    public GoalProgressSummaryDTO getGoalProgressSummary(UUID userId) {
        long active    = goalRepository.countByUserIdAndStatusAndIsDeletedFalse(userId, "ACTIVE");
        long completed = goalRepository.countByUserIdAndStatusAndIsDeletedFalse(userId, "COMPLETED");
        List<GoalEntity> topGoals = goalRepository.findTopGoalsByProgress(
            userId, PageRequest.of(0, 3));
        double avgProgress = topGoals.stream()
            .mapToDouble(g -> g.getProgressPct().doubleValue())
            .average().orElse(0.0);

        return GoalProgressSummaryDTO.builder()
            .activeGoals((int) active)
            .completedGoals((int) completed)
            .avgProgressPct(avgProgress)
            .build();
    }

    public List<GoalTimelineEvent> getGoalTimeline(UUID userId, UUID goalId) {
        // Validate ownership
        GoalEntity goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found"));

        List<GoalTimelineEvent> events = new java.util.ArrayList<>();

        // 1. Goal created event
        events.add(GoalTimelineEvent.builder()
                .id(goal.getId())
                .eventType("GOAL_CREATED")
                .title("Goal started: " + goal.getTitle())
                .occurredAt(goal.getCreatedAt())
                .build());

        // 2. Completed todos
        todoRepository.findCompletedByGoalId(goalId, userId).forEach(t ->
                events.add(GoalTimelineEvent.builder()
                        .id(t.getId())
                        .eventType("TODO_COMPLETED")
                        .title(t.getTitle())
                        .occurredAt(t.getCompletedAt())
                        .build()));

        // 3. Completed milestones
        milestoneRepository.findByGoalIdAndCompletedTrueOrderByCompletedAtDesc(goalId).forEach(m ->
                events.add(GoalTimelineEvent.builder()
                        .id(m.getId())
                        .eventType("MILESTONE_COMPLETED")
                        .title(m.getTitle())
                        .occurredAt(m.getCompletedAt())
                        .build()));

        // 4. Logged activities (latest 20 to keep the timeline readable)
        activityRepository.findByGoalIdAndUserId(goalId, userId,
                        org.springframework.data.domain.PageRequest.of(0, 20))
                .forEach(a -> {
                    ActivityTypeEntity type = activityTypeRepository
                            .findById(a.getActivityTypeId()).orElse(null);
                    events.add(GoalTimelineEvent.builder()
                            .id(a.getId())
                            .eventType("ACTIVITY_LOGGED")
                            .title(type != null ? type.getName() + " logged" : "Activity logged")
                            .occurredAt(a.getLoggedAt())
                            .build());
                });

        // Sort all events newest first — nulls (e.g. todo without completedAt) go to end
        events.sort(java.util.Comparator.comparing(
                GoalTimelineEvent::getOccurredAt,
                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        return events;
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Recalculate goal progress = (completedTodos / totalTodos) * 100.
     * Called atomically within the same @Transactional as todo toggle.
     */
    private void recalculateGoalProgress(UUID goalId) {
        long total     = todoRepository.countByGoalIdAndIsDeletedFalse(goalId);
        long completed = todoRepository.countByGoalIdAndCompletedTrueAndIsDeletedFalse(goalId);
        if (total == 0) {
            goalRepository.findByIdAndIsDeletedFalse(goalId).ifPresent(goal -> {
                goal.setProgressPct(BigDecimal.ZERO);
                if ("COMPLETED".equals(goal.getStatus())) {
                    goal.setStatus("ACTIVE");
                }
                goalRepository.save(goal);
            });
            return;
        }

        BigDecimal progress = BigDecimal.valueOf(completed)
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);

        goalRepository.findByIdAndIsDeletedFalse(goalId).ifPresent(goal -> {
            goal.setProgressPct(progress);
            if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
                goal.setStatus("COMPLETED");
            }
            goalRepository.save(goal);
        });
    }

    private void validateGoalOwnership(UUID userId, UUID goalId) {
        goalRepository.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new GoalNotFoundException("Goal not found: " + goalId));
    }

    // ─────────────────────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────────────────────

    GoalResponse toGoalResponse(GoalEntity g) {
        long total     = todoRepository.countByGoalIdAndIsDeletedFalse(g.getId());
        long completed = todoRepository.countByGoalIdAndCompletedTrueAndIsDeletedFalse(g.getId());
        List<MilestoneResponse> milestones = milestoneRepository
            .findByGoalIdOrderByTargetDateAsc(g.getId())
            .stream().map(this::toMilestoneResponse).collect(Collectors.toList());

        GoalTypeDTO typeDTO = goalTypeRepository.findById(g.getCategoryTypeId())
            .map(this::toGoalTypeDTO).orElse(null);

        return GoalResponse.builder()
            .id(g.getId())
            .title(g.getTitle())
            .categoryType(typeDTO)
            .targetDate(g.getTargetDate())
            .description(g.getDescription())
            .progressPct(g.getProgressPct())
            .status(g.getStatus())
            .color(g.getColor())
            .icon(g.getIcon())
            .todoCount((int) total)
            .completedTodos((int) completed)
            .milestones(milestones)
            .createdAt(g.getCreatedAt())
            .updatedAt(g.getUpdatedAt())
            .build();
    }

    private TodoResponse toTodoResponse(TodoEntity t) {
        return TodoResponse.builder()
            .id(t.getId())
            .goalId(t.getGoalId())
            .title(t.getTitle())
            .description(t.getDescription())
            .dueDate(t.getDueDate())
            .dueTime(t.getDueTime() != null ? t.getDueTime().toString() : null)
            .completed(t.isCompleted())
            .completedAt(t.getCompletedAt())
            .recurrenceRule(t.getRecurrenceRule())
            .calendarEventId(t.getCalendarEventId())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }

    private MilestoneResponse toMilestoneResponse(MilestoneEntity m) {
        return MilestoneResponse.builder()
            .id(m.getId())
            .title(m.getTitle())
            .targetDate(m.getTargetDate())
            .completed(m.isCompleted())
            .completedAt(m.getCompletedAt())
            .build();
    }

    private GoalTypeDTO toGoalTypeDTO(GoalTypeEntity gt) {
        return GoalTypeDTO.builder()
            .id(gt.getId())
            .name(gt.getName())
            .icon(gt.getIcon())
            .color(gt.getColor())
            .unit(gt.getUnit())
            .isSystem(gt.isSystem())
            .isPublic(gt.isPublic())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
// ACTIVITY TYPES (Type Registry)
// ─────────────────────────────────────────────────────────────

    public List<ActivityTypeDTO> listActivityTypes(UUID userId) {
        return activityTypeRepository.findAvailableForUser(userId)
                .stream().map(this::toActivityTypeDTO).collect(Collectors.toList());
    }

    @Transactional
    public ActivityTypeDTO createActivityType(UUID userId, ActivityTypeRequest request) {
        if (activityTypeRepository.existsByNameAndCreatedBy(request.getName(), userId)) {
            throw new DuplicateResourceException("DUPLICATE_TYPE",
                    "You already have an activity type named '" + request.getName() + "'");
        }
        ActivityTypeEntity entity = ActivityTypeEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .color(request.getColor())
                .isSystem(false)
                .createdBy(userId)
                .build();
        return toActivityTypeDTO(activityTypeRepository.save(entity));
    }

    @Transactional
    public ActivityTypeDTO updateActivityType(UUID userId, UUID typeId, ActivityTypeRequest request) {
        ActivityTypeEntity entity = activityTypeRepository.findByIdAndCreatedBy(typeId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Activity type not found or not editable"));
        if (request.getName() != null) entity.setName(request.getName().trim());
        if (request.getIcon()  != null) entity.setIcon(request.getIcon());
        if (request.getColor() != null) entity.setColor(request.getColor());
        return toActivityTypeDTO(activityTypeRepository.save(entity));
    }

    @Transactional
    public void deleteActivityType(UUID userId, UUID typeId) {
        ActivityTypeEntity entity = activityTypeRepository.findByIdAndCreatedBy(typeId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Activity type not found or not editable"));
        activityTypeRepository.delete(entity);
    }

// ─────────────────────────────────────────────────────────────
// ACTIVITIES
// ─────────────────────────────────────────────────────────────

    public PageResponse<ActivityResponse> listActivities(UUID userId, UUID goalId, Pageable pageable) {
        // Verify goal belongs to this user
        goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found"));
        return PageResponse.of(
                activityRepository.findByGoalIdAndUserId(goalId, userId, pageable)
                        .map(this::toActivityResponse));
    }

    @Transactional
    public ActivityResponse logActivity(UUID userId, UUID goalId, ActivityRequest request) {
        goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found"));

        ActivityTypeEntity type = activityTypeRepository.findById(request.getActivityTypeId())
                .orElseThrow(() -> new InvalidRequestException("NOT_FOUND","Activity type not found"));

        ActivityEntity entity = ActivityEntity.builder()
                .userId(userId)
                .goalId(goalId)
                .activityTypeId(type.getId())
                .note(request.getNote())
                .value(request.getValue())
                .unit(request.getUnit())
                .build();
        return toActivityResponse(activityRepository.save(entity));
    }

    @Transactional
    public void deleteActivity(UUID userId, UUID goalId, UUID activityId) {
        ActivityEntity entity = activityRepository.findByIdAndUserId(activityId, userId)
                .orElseThrow(() -> new GoalNotFoundException("Activity not found"));
        // Ensure it belongs to the requested goal too
        if (!entity.getGoalId().equals(goalId)) {
            throw new AccessDeniedException("Activity does not belong to this goal");
        }
        entity.setDeleted(true);
        activityRepository.save(entity);
    }

// ─────────────────────────────────────────────────────────────
// ACTIVITY MAPPERS (private)
// ─────────────────────────────────────────────────────────────

    private ActivityResponse toActivityResponse(ActivityEntity e) {
        // Fetch type name+icon for the response (type will be in 1st-level cache after initial load)
        ActivityTypeEntity type = activityTypeRepository.findById(e.getActivityTypeId()).orElse(null);
        return ActivityResponse.builder()
                .id(e.getId())
                .goalId(e.getGoalId())
                .activityTypeName(type != null ? type.getName() : null)
                .activityTypeIcon(type != null ? type.getIcon() : null)
                .note(e.getNote())
                .value(e.getValue())
                .unit(e.getUnit())
                .loggedAt(e.getLoggedAt())
                .build();
    }

    private ActivityTypeDTO toActivityTypeDTO(ActivityTypeEntity e) {
        return ActivityTypeDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .icon(e.getIcon())
                .color(e.getColor())
                .isSystem(e.isSystem())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
