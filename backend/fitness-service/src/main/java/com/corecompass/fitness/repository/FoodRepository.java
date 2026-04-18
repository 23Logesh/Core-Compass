package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<FoodEntity, UUID> {

    // System foods + user's own, optionally filtered by name search
    @Query("""
        SELECT f FROM FoodEntity f
        WHERE f.isDeleted = false
          AND (f.isSystem = true OR f.createdBy = :userId)
          AND (:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY f.isSystem DESC, f.name ASC
        """)
    List<FoodEntity> findAvailable(
            @Param("userId") UUID userId,
            @Param("search") String search
    );

    // Single food — visible if system or owned
    @Query("""
        SELECT f FROM FoodEntity f
        WHERE f.id = :id AND f.isDeleted = false
          AND (f.isSystem = true OR f.createdBy = :userId)
        """)
    Optional<FoodEntity> findByIdAndVisible(
            @Param("id") UUID id, @Param("userId") UUID userId
    );

    // Edit/delete — must be owned
    Optional<FoodEntity> findByIdAndCreatedByAndIsDeletedFalse(UUID id, UUID createdBy);
}