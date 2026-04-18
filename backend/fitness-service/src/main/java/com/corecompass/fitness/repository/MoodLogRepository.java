package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MoodLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLogEntity, UUID> {

    List<MoodLogEntity> findByUserIdAndLoggedDateOrderByCreatedAtDesc(UUID userId, LocalDate date);
    // ADD after existing method:

    Page<MoodLogEntity> findByUserIdOrderByLoggedDateDesc(UUID userId, Pageable pageable);

    Optional<MoodLogEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT m.mood FROM MoodLogEntity m " +
            "WHERE m.userId=:u AND YEAR(m.loggedDate)=:yr AND MONTH(m.loggedDate)=:mo")
    List<String> findMoodsForMonth(@Param("u") UUID u, @Param("yr") int yr, @Param("mo") int mo);
}
