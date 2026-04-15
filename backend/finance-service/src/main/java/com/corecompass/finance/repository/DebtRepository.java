package com.corecompass.finance.repository;

import com.corecompass.finance.entity.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<DebtEntity, UUID> {
    List<DebtEntity> findByUserIdAndIsDeletedFalseOrderByCurrentBalanceDesc(UUID userId);

    Optional<DebtEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(d.currentBalance),0) FROM DebtEntity d WHERE d.userId=:u AND d.isDeleted=false")
    BigDecimal sumCurrentBalanceByUser(@Param("u") UUID u);
}
