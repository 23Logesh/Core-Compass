package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutSessionEntity, UUID> {

    Page<WorkoutSessionEntity> findByUserIdAndIsDeletedFalseOrderBySessionDateDesc(UUID userId, Pageable p);

    Optional<WorkoutSessionEntity> findByIdAndUserId(UUID id, UUID userId);

    // Matches entity field: sessionDate
    int countByUserIdAndSessionDateBetweenAndIsDeletedFalse(UUID userId, LocalDate s, LocalDate e);

    @Query("SELECT DISTINCT w.sessionDate FROM WorkoutSessionEntity w " +
            "WHERE w.userId=:u AND w.isDeleted=false ORDER BY w.sessionDate DESC")
    List<LocalDate> findDistinctWorkoutDates(@Param("u") UUID u);
}