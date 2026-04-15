package com.corecompass.habits.repository;

import com.corecompass.habits.entity.HabitStackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitStackRepository extends JpaRepository<HabitStackEntity, UUID> {
    List<HabitStackEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<HabitStackEntity> findByIdAndUserId(UUID id, UUID userId);
}
