package com.corecompass.core.repository;

import com.corecompass.core.entity.GoalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// GOAL REPOSITORY
// ─────────────────────────────────────────────────────────────
@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {

    // Paginated goals for a user — with optional status filter
    @Query("""
                SELECT g FROM GoalEntity g
                WHERE g.userId = :userId
                  AND g.isDeleted = false
                  AND (:status IS NULL OR g.status = :status)
                ORDER BY g.createdAt DESC
            """)
    Page<GoalEntity> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") String status,
            Pageable pageable
    );

    // Goal with ownership check
    @Query("SELECT g FROM GoalEntity g WHERE g.id = :id AND g.userId = :userId AND g.isDeleted = false")
    Optional<GoalEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count active goals (for dashboard)
    long countByUserIdAndStatusAndIsDeletedFalse(UUID userId, String status);

    // Top N goals by progress (dashboard widget)
    @Query("""
                SELECT g FROM GoalEntity g
                WHERE g.userId = :userId AND g.status = 'ACTIVE' AND g.isDeleted = false
                ORDER BY g.progressPct DESC
            """)
    List<GoalEntity> findTopGoalsByProgress(@Param("userId") UUID userId, Pageable pageable);

    // 90-day heatmap: goals that had todo completions per day
    @Query(value = """
                SELECT DATE(t.completed_at) as date, COUNT(*) as count
                FROM core_schema.todos t
                WHERE t.user_id = :userId
                  AND t.completed = true
                  AND t.completed_at >= NOW() - INTERVAL '90 days'
                  AND t.is_deleted = false
                GROUP BY DATE(t.completed_at)
                ORDER BY date ASC
            """, nativeQuery = true)
    List<Object[]> findHeatmapData(@Param("userId") UUID userId);

    // All active userIds (used by Report Service via Feign)
    @Query("SELECT DISTINCT g.userId FROM GoalEntity g WHERE g.status = 'ACTIVE' AND g.isDeleted = false")
    List<UUID> findActiveUserIds();

    Optional<GoalEntity> findByIdAndIsDeletedFalse(UUID goalId);
}
