package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MetricTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetricTypeRepository extends JpaRepository<MetricTypeEntity, UUID> {

    @Query("SELECT t FROM MetricTypeEntity t " +
            "WHERE t.isSystem = true OR t.createdBy = :userId " +
            "ORDER BY t.isSystem DESC, t.name ASC")
    List<MetricTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<MetricTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}