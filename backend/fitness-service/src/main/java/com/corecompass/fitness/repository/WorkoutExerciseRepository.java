package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.WorkoutExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExerciseEntity, UUID> {

    List<WorkoutExerciseEntity> findBySessionIdOrderBySetNumberAsc(UUID sessionId);
}