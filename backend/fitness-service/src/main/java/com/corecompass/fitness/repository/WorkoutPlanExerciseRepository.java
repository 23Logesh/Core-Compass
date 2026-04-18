package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.WorkoutPlanExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutPlanExerciseRepository extends JpaRepository<WorkoutPlanExerciseEntity, UUID> {

    List<WorkoutPlanExerciseEntity> findByPlanIdOrderByDayNumberAscSortOrderAsc(UUID planId);

    void deleteByPlanId(UUID planId);
}