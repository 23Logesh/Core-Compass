package com.corecompass.fitness.repository;

import com.corecompass.fitness.entity.DietPlanMealEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DietPlanMealRepository extends JpaRepository<DietPlanMealEntity, UUID> {

    List<DietPlanMealEntity> findByPlanIdOrderByDayNumberAscMealTypeAscSortOrderAsc(UUID planId);

    void deleteByPlanId(UUID planId);
}