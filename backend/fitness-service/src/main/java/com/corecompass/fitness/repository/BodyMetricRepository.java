package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.BodyMetricEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BodyMetricRepository extends JpaRepository<BodyMetricEntity, UUID> {
    @Query("SELECT b FROM BodyMetricEntity b WHERE b.userId=:u AND (:t IS NULL OR b.metricType=:t) ORDER BY b.loggedDate DESC")
    Page<BodyMetricEntity> findByUserIdAndMetricType(@Param("u") UUID u, @Param("t") String t, Pageable p);
}
