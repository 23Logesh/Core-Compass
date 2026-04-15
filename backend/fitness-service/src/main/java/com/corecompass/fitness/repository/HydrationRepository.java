package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.HydrationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface HydrationRepository extends JpaRepository<HydrationLogEntity, UUID> {
    @Query("SELECT COALESCE(SUM(h.amountMl),0) FROM HydrationLogEntity h WHERE h.userId=:u AND h.loggedDate=:d")
    int sumAmountForDay(@Param("u") UUID u, @Param("d") LocalDate d);
}
