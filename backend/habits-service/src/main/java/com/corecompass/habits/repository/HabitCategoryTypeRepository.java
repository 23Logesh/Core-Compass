package com.corecompass.habits.repository;

import com.corecompass.habits.entity.HabitCategoryTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitCategoryTypeRepository extends JpaRepository<HabitCategoryTypeEntity, UUID> {
    @Query("SELECT c FROM HabitCategoryTypeEntity c WHERE c.isSystem=true OR c.createdBy=:u ORDER BY c.name ASC")
    List<HabitCategoryTypeEntity> findAvailableForUser(@Param("u") UUID u);

    Optional<HabitCategoryTypeEntity> findByIdAndCreatedBy(UUID id, UUID createdBy);
}
