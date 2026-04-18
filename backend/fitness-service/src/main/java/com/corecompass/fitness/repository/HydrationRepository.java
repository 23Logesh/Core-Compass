package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.HydrationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface HydrationRepository extends JpaRepository<HydrationLogEntity, UUID> {

    @Query("SELECT COALESCE(SUM(h.amountMl),0) FROM HydrationLogEntity h WHERE h.userId=:u AND h.loggedDate=:d")
    int sumAmountForDay(@Param("u") UUID u, @Param("d") LocalDate d);
    // ADD after existing method:

    Page<HydrationLogEntity> findByUserIdOrderByLoggedDateDesc(UUID userId, Pageable pageable);

    Optional<HydrationLogEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(h.amountMl),0) FROM HydrationLogEntity h " +
            "WHERE h.userId=:u AND YEAR(h.loggedDate)=:yr AND MONTH(h.loggedDate)=:mo")
    int sumAmountForMonth(@Param("u") UUID u, @Param("yr") int yr, @Param("mo") int mo);
}
