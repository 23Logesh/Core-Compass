package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.DietPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlanEntity, UUID> {

    List<DietPlanEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<DietPlanEntity> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    Optional<DietPlanEntity> findByUserIdAndIsActiveTrueAndIsDeletedFalse(UUID userId);

    @Modifying
    @Query("UPDATE DietPlanEntity p SET p.isActive = false " +
            "WHERE p.userId = :userId AND p.isActive = true")
    void deactivateAllForUser(@Param("userId") UUID userId);
}