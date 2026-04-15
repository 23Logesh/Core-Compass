package com.corecompass.habits.repository;
import com.corecompass.habits.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface RoutineGroupRepository extends JpaRepository<RoutineGroupEntity, UUID> {
    List<RoutineGroupEntity> findByUserIdAndIsDeletedFalseOrderByTimeOfDayAsc(UUID userId);
    Optional<RoutineGroupEntity> findByIdAndUserId(UUID id, UUID userId);
}

