package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.CardioLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface CardioLogRepository extends JpaRepository<CardioLogEntity, UUID> {
    // FIX: was referencing isDeleted which now exists on the entity
    Page<CardioLogEntity> findByUserIdAndIsDeletedFalseOrderByLoggedDateDesc(UUID userId, Pageable p);

    @Query("SELECT COALESCE(SUM(c.caloriesBurned),0) FROM CardioLogEntity c " +
            "WHERE c.userId=:u AND c.loggedDate BETWEEN :s AND :e AND c.isDeleted=false")
    int sumCaloriesBurned(@Param("u") UUID u, @Param("s") LocalDate s, @Param("e") LocalDate e);

    Page<CardioLogEntity> findByUserIdAndCardioTypeAndIsDeletedFalseOrderByLoggedDateDesc(UUID userId, String cardioType, Pageable p);
}
