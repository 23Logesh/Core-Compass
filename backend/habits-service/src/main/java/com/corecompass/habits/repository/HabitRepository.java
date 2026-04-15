package com.corecompass.habits.repository;

import com.corecompass.habits.entity.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {
    List<HabitEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<HabitEntity> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    @Query("SELECT COUNT(h) FROM HabitEntity h WHERE h.userId=:u AND h.status='ACTIVE' AND h.isDeleted=false")
    long countActiveByUser(@Param("u") UUID u);
}
