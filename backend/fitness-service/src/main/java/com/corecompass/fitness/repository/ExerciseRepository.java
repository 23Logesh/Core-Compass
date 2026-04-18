package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, UUID> {

    // All system exercises + this user's custom exercises, filtered by muscleGroup if provided
    @Query("""
        SELECT e FROM ExerciseEntity e
        WHERE e.isDeleted = false
          AND (e.isSystem = true OR e.createdBy = :userId)
          AND (:muscleGroup IS NULL OR e.muscleGroup = :muscleGroup)
          AND (:equipment IS NULL OR e.equipment = :equipment)
          AND (:difficulty IS NULL OR e.difficulty = :difficulty)
        ORDER BY e.isSystem DESC, e.name ASC
        """)
    List<ExerciseEntity> findAvailable(
            @Param("userId")      UUID   userId,
            @Param("muscleGroup") String muscleGroup,
            @Param("equipment")   String equipment,
            @Param("difficulty")  String difficulty
    );

    // Fetch single — visible if system or owned by this user
    @Query("""
        SELECT e FROM ExerciseEntity e
        WHERE e.id = :id
          AND e.isDeleted = false
          AND (e.isSystem = true OR e.createdBy = :userId)
        """)
    Optional<ExerciseEntity> findByIdAndVisible(
            @Param("id") UUID id, @Param("userId") UUID userId
    );

    // For edit/delete — must be owned (not system)
    Optional<ExerciseEntity> findByIdAndCreatedByAndIsDeletedFalse(UUID id, UUID createdBy);
}