package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.SupplementLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementLogRepository extends JpaRepository<SupplementLogEntity, UUID> {

    // Paginated history
    Page<SupplementLogEntity> findByUserIdAndIsDeletedFalseOrderByLoggedDateDescCreatedAtDesc(
            UUID userId, Pageable pageable);

    // Today's logs
    List<SupplementLogEntity> findByUserIdAndLoggedDateAndIsDeletedFalse(
            UUID userId, LocalDate date);

    // Ownership-safe single fetch
    Optional<SupplementLogEntity> findByIdAndUserId(UUID id, UUID userId);
}