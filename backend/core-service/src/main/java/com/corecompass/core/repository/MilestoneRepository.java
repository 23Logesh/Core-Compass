package com.corecompass.core.repository;

import com.corecompass.core.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// MILESTONE REPOSITORY
// ─────────────────────────────────────────────────────────────
@Repository
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, UUID> {

    List<MilestoneEntity> findByGoalIdOrderByTargetDateAsc(UUID goalId);

    Optional<MilestoneEntity> findByIdAndUserId(UUID id, UUID userId);

    // Completed milestones for timeline
    List<MilestoneEntity> findByGoalIdAndCompletedTrueOrderByCompletedAtDesc(UUID goalId);
}
