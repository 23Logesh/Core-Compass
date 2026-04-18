package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MealLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface MealLogRepository extends JpaRepository<MealLogEntity, UUID> {

    List<MealLogEntity> findByUserIdAndMealDateAndIsDeletedFalse(UUID userId, LocalDate date);
    Page<MealLogEntity> findByUserIdAndIsDeletedFalse(UUID userId, Pageable p);

    Optional<MealLogEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(m.totalCalories),0) FROM MealLogEntity m " +
            "WHERE m.userId=:u AND YEAR(m.mealDate)=:yr AND MONTH(m.mealDate)=:mo AND m.isDeleted=false")
    long sumCaloriesForMonth(@Param("u") UUID u, @Param("yr") int yr, @Param("mo") int mo);

    @Query("SELECT COUNT(DISTINCT m.mealDate) FROM MealLogEntity m " +
            "WHERE m.userId=:u AND YEAR(m.mealDate)=:yr AND MONTH(m.mealDate)=:mo AND m.isDeleted=false")
    long countDistinctDaysForMonth(@Param("u") UUID u, @Param("yr") int yr, @Param("mo") int mo);

}