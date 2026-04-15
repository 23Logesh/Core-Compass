package com.corecompass.finance.repository;

import com.corecompass.finance.entity.ExpenseCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, UUID> {
    @Query("SELECT c FROM ExpenseCategoryEntity c WHERE c.isSystem=true OR c.createdBy=:u ORDER BY c.name ASC")
    List<ExpenseCategoryEntity> findAvailableForUser(@Param("u") UUID u);

    List<ExpenseCategoryEntity> findByIsSystemTrue();
}
