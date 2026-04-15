package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MoodLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLogEntity, UUID> {
    List<MoodLogEntity> findByUserIdAndLoggedDateOrderByCreatedAtDesc(UUID userId, LocalDate date);
}
