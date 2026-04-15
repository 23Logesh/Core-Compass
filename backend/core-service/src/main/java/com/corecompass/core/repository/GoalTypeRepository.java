package com.corecompass.core.repository;

import com.corecompass.core.entity.GoalTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// GOAL TYPE REPOSITORY
// ─────────────────────────────────────────────────────────────
@Repository
public interface GoalTypeRepository extends JpaRepository<GoalTypeEntity, UUID> {

    // System types + user's own custom types
    @Query("""
                SELECT gt FROM GoalTypeEntity gt
                WHERE gt.isSystem = true
                   OR gt.createdBy = :userId
                   OR gt.isPublic = true
                ORDER BY gt.isSystem DESC, gt.name ASC
            """)
    List<GoalTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<GoalTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}
