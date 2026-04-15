package com.corecompass.finance.repository;

import com.corecompass.finance.entity.InvestmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvestmentTypeRepository extends JpaRepository<InvestmentTypeEntity, UUID> {
    @Query("SELECT t FROM InvestmentTypeEntity t WHERE t.isSystem=true OR t.createdBy=:u ORDER BY t.name ASC")
    List<InvestmentTypeEntity> findAvailableForUser(@Param("u") UUID u);
}
