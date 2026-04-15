package com.corecompass.finance.repository;

import com.corecompass.finance.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    List<BudgetEntity> findByUserIdAndBudgetMonth(UUID userId, String budgetMonth);

    Optional<BudgetEntity> findByUserIdAndCategoryIdAndBudgetMonth(UUID userId, UUID categoryId, String budgetMonth);
}
