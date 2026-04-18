package com.corecompass.finance.repository;

import com.corecompass.finance.entity.IncomeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, UUID> {
    Page<IncomeEntity> findByUserIdAndIsDeletedFalseOrderByIncomeDateDesc(UUID userId, Pageable p);

    Optional<IncomeEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(i.amount),0) FROM IncomeEntity i WHERE i.userId=:u AND i.isDeleted=false AND i.incomeDate BETWEEN :s AND :e")
    BigDecimal sumByUserAndDateRange(@Param("u") UUID u, @Param("s") LocalDate s, @Param("e") LocalDate e);

    // Distinct source types the user has logged, for income/sources endpoint
    @Query("SELECT i.sourceType, COUNT(i), SUM(i.amount) FROM IncomeEntity i " +
            "WHERE i.userId=:u AND i.isDeleted=false GROUP BY i.sourceType ORDER BY COUNT(i) DESC")
    List<Object[]> findSourceSummaries(@Param("u") UUID u);

}
