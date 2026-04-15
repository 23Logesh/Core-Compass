package com.corecompass.finance.repository;

import com.corecompass.finance.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentRepository extends JpaRepository<InvestmentEntity, UUID> {
    List<InvestmentEntity> findByUserIdAndIsDeletedFalseOrderByPurchaseDateDesc(UUID userId);

    Optional<InvestmentEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(i.investedAmount),0), COALESCE(SUM(i.currentValue),0) FROM InvestmentEntity i WHERE i.userId=:u AND i.isDeleted=false")
    Object[] getSummaryByUser(@Param("u") UUID u);
}
