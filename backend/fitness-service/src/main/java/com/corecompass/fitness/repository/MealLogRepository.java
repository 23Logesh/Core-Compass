package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MealLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealLogRepository extends JpaRepository<MealLogEntity, UUID> {
    List<MealLogEntity> findByUserIdAndMealDateAndIsDeletedFalse(UUID userId, LocalDate date);
    Page<MealLogEntity> findByUserIdAndIsDeletedFalse(UUID userId, Pageable p);
}