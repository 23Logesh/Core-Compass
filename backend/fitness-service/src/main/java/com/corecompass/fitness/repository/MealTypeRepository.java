package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.MealTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealTypeRepository extends JpaRepository<MealTypeEntity, UUID> {

    @Query("SELECT t FROM MealTypeEntity t " +
            "WHERE t.isSystem = true OR t.createdBy = :userId " +
            "ORDER BY t.isSystem DESC, t.name ASC")
    List<MealTypeEntity> findAvailableForUser(@Param("userId") UUID userId);

    Optional<MealTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);
}