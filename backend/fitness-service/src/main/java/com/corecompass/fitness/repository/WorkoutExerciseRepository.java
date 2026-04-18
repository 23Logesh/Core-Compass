package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.WorkoutExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExerciseEntity, UUID> {

    List<WorkoutExerciseEntity> findBySessionIdOrderBySetNumberAsc(UUID sessionId);

    // ADD after existing findBySessionIdOrderBySetNumberAsc:

    void deleteBySessionId(UUID sessionId);

    @Query("SELECT e FROM WorkoutExerciseEntity e " +
            "JOIN WorkoutSessionEntity w ON e.sessionId = w.id " +
            "WHERE w.userId = :u AND w.isDeleted = false")
    List<WorkoutExerciseEntity> findAllByUserId(@Param("u") UUID userId);

}