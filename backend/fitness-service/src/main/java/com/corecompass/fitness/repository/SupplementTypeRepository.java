package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.SupplementTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementTypeRepository extends JpaRepository<SupplementTypeEntity, UUID> {

    // System types + user's own custom types
    @Query("SELECT t FROM SupplementTypeEntity t " +
            "WHERE t.isSystem = true OR t.createdBy = :userId " +
            "ORDER BY t.isSystem DESC, t.category ASC, t.name ASC")
    List<SupplementTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<SupplementTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}