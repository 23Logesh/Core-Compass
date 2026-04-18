package com.corecompass.finance.repository;

import com.corecompass.finance.entity.RecurringExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpenseEntity, UUID> {

    List<RecurringExpenseEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<RecurringExpenseEntity> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);
}