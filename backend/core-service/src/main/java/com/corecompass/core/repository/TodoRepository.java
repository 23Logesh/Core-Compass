package com.corecompass.core.repository;

import com.corecompass.core.entity.TodoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// TODO REPOSITORY
// ─────────────────────────────────────────────────────────────
@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {

    @Query("""
                SELECT t FROM TodoEntity t
                WHERE t.goalId = :goalId
                  AND t.userId = :userId
                  AND t.isDeleted = false
                  AND (:status IS NULL
                       OR (:status = 'COMPLETED' AND t.completed = true)
                       OR (:status = 'PENDING'   AND t.completed = false))
                ORDER BY t.dueDate ASC NULLS LAST, t.createdAt DESC
            """)
    Page<TodoEntity> findByGoalIdAndUserId(
            @Param("goalId") UUID goalId,
            @Param("userId") UUID userId,
            @Param("status") String status,
            Pageable pageable
    );

    // All todos due today across all goals
    @Query("""
                SELECT t FROM TodoEntity t
                WHERE t.userId = :userId
                  AND t.dueDate = :today
                  AND t.isDeleted = false
                ORDER BY t.completed ASC, t.createdAt ASC
            """)
    List<TodoEntity> findTodaysTodos(@Param("userId") UUID userId, @Param("today") LocalDate today);

    @Query("SELECT t FROM TodoEntity t WHERE t.id = :id AND t.userId = :userId AND t.isDeleted = false")
    Optional<TodoEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count for goal progress recalculation
    long countByGoalIdAndIsDeletedFalse(UUID goalId);

    long countByGoalIdAndCompletedTrueAndIsDeletedFalse(UUID goalId);

    // Todos pending calendar sync (retry queue)
    @Query("""
                SELECT t FROM TodoEntity t
                WHERE t.calendarEventId IS NULL
                  AND t.dueDate IS NOT NULL
                  AND t.dueTime IS NOT NULL
                  AND t.calendarSyncAttempts < 3
                  AND t.isDeleted = false
                ORDER BY t.createdAt ASC
            """)
    List<TodoEntity> findPendingCalendarSync();

    // Duplicate check
    @Query("""
                SELECT COUNT(t) > 0 FROM TodoEntity t
                WHERE t.goalId = :goalId
                  AND t.userId = :userId
                  AND t.title = :title
                  AND t.dueDate = :dueDate
                  AND t.isDeleted = false
            """)
    boolean existsDuplicate(
            @Param("goalId") UUID goalId,
            @Param("userId") UUID userId,
            @Param("title") String title,
            @Param("dueDate") LocalDate dueDate
    );

    long countByUserIdAndCompletedTrue(UUID userId);

    // Completed todos for a specific goal — timeline use
    @Query("SELECT t FROM TodoEntity t WHERE t.goalId = :goalId AND t.userId = :userId " +
            "AND t.completed = true AND t.isDeleted = false ORDER BY t.completedAt DESC")
    List<TodoEntity> findCompletedByGoalId(@Param("goalId") UUID goalId,
                                           @Param("userId") UUID userId);
}
