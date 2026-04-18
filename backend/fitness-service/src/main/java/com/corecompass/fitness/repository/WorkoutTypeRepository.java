package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.WorkoutTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutTypeEntity, UUID> {

    @Query("SELECT t FROM WorkoutTypeEntity t " +
            "WHERE t.isSystem = true OR t.createdBy = :userId " +
            "ORDER BY t.isSystem DESC, t.name ASC")
    List<WorkoutTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<WorkoutTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}