package com.corecompass.finance.repository;
import com.corecompass.finance.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, UUID> {
    List<SavingsGoalEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);
    Optional<SavingsGoalEntity> findByIdAndUserId(UUID id, UUID userId);
}

