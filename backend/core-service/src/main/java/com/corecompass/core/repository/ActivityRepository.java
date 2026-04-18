package com.corecompass.core.repository;

import com.corecompass.core.entity.ActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, UUID> {

    // All activities for a goal, newest first
    @Query("SELECT a FROM ActivityEntity a WHERE a.goalId = :goalId AND a.userId = :userId AND a.isDeleted = false ORDER BY a.loggedAt DESC")
    Page<ActivityEntity> findByGoalIdAndUserId(@Param("goalId") UUID goalId, @Param("userId") UUID userId, Pageable pageable);

    // Ownership-safe single fetch
    Optional<ActivityEntity> findByIdAndUserId(UUID id, UUID userId);
}