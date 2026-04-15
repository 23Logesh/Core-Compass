package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.SleepLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLogEntity, UUID> {
    @Query("SELECT COALESCE(AVG(s.durationHours),0) FROM SleepLogEntity s " +
            "WHERE s.userId=:u AND s.sleepDate BETWEEN :s AND :e")
    double avgDurationHours(@Param("u") UUID u, @Param("s") LocalDate s, @Param("e") LocalDate e);
}
