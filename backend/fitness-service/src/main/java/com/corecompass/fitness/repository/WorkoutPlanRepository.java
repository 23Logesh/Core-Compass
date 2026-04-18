package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.WorkoutPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlanEntity, UUID> {

    List<WorkoutPlanEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<WorkoutPlanEntity> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    Optional<WorkoutPlanEntity> findByUserIdAndIsActiveTrueAndIsDeletedFalse(UUID userId);

    // Deactivate ALL plans for this user — called before activating a new one
    @Modifying
    @Query("UPDATE WorkoutPlanEntity p SET p.isActive = false " +
            "WHERE p.userId = :userId AND p.isActive = true")
    void deactivateAllForUser(@Param("userId") UUID userId);
}