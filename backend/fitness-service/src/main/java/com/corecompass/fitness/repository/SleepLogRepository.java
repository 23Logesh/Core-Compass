package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.SleepLogEntity;
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
public interface SleepLogRepository extends JpaRepository<SleepLogEntity, UUID> {

    @Query("SELECT COALESCE(AVG(s.durationHours),0) FROM SleepLogEntity s " +
            "WHERE s.userId=:u AND s.sleepDate BETWEEN :s AND :e")
    double avgDurationHours(@Param("u") UUID u, @Param("s") LocalDate s, @Param("e") LocalDate e);

    Page<SleepLogEntity> findByUserIdOrderBySleepDateDesc(UUID userId, Pageable pageable);

    Optional<SleepLogEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(AVG(s.durationHours),0) FROM SleepLogEntity s " +
            "WHERE s.userId=:u AND YEAR(s.sleepDate)=:yr AND MONTH(s.sleepDate)=:mo")
    double avgDurationHoursForMonth(@Param("u") UUID u, @Param("yr") int yr, @Param("mo") int mo);
}
