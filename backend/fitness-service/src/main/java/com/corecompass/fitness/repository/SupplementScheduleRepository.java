package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.SupplementScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementScheduleRepository extends JpaRepository<SupplementScheduleEntity, UUID> {

    List<SupplementScheduleEntity> findByUserIdAndIsActiveTrueOrderByTimingAsc(UUID userId);

    Optional<SupplementScheduleEntity> findByIdAndUserId(UUID id, UUID userId);
}