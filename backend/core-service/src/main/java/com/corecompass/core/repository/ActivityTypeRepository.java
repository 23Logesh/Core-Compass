package com.corecompass.core.repository;

import com.corecompass.core.entity.ActivityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityTypeEntity, UUID> {

    // System types + user's own custom types
    @Query("SELECT t FROM ActivityTypeEntity t WHERE t.isSystem = true OR t.createdBy = :userId ORDER BY t.isSystem DESC, t.name ASC")
    List<ActivityTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<ActivityTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}