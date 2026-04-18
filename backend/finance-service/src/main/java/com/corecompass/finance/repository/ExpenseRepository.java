package com.corecompass.finance.repository;

import com.corecompass.finance.entity.ExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {
    @Query("SELECT e FROM ExpenseEntity e WHERE e.userId=:u AND e.isDeleted=false " +
            "AND (:cat IS NULL OR e.categoryId=:cat) " +
            "AND (:from IS NULL OR e.expenseDate>=:from) " +
            "AND (:to IS NULL OR e.expenseDate<=:to) ORDER BY e.expenseDate DESC")
    Page<ExpenseEntity> findFiltered(@Param("u") UUID u, @Param("cat") UUID cat,
                                     @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable p);

    Optional<ExpenseEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM ExpenseEntity e WHERE e.userId=:u AND e.isDeleted=false AND e.expenseDate BETWEEN :s AND :e")
    BigDecimal sumByUserAndDateRange(@Param("u") UUID u, @Param("s") LocalDate s, @Param("e") LocalDate e);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM ExpenseEntity e WHERE e.userId=:u AND e.categoryId=:cat AND e.isDeleted=false AND e.expenseDate BETWEEN :s AND :end")
    BigDecimal sumByCategoryAndDateRange(@Param("u") UUID u, @Param("cat") UUID cat, @Param("s") LocalDate s, @Param("end") LocalDate end);

    // Spending patterns - weekend vs weekday
    @Query(value = "SELECT EXTRACT(DOW FROM expense_date) as dow, SUM(amount) as total " +
            "FROM finance_schema.expenses WHERE user_id=:u AND is_deleted=false " +
            "AND expense_date >= :from GROUP BY dow ORDER BY dow", nativeQuery = true)
    List<Object[]> getDayOfWeekSpending(@Param("u") UUID u, @Param("from") LocalDate from);

    // Top merchants
    @Query("SELECT e.merchant, SUM(e.amount) as total FROM ExpenseEntity e " +
            "WHERE e.userId=:u AND e.isDeleted=false AND e.merchant IS NOT NULL " +
            "AND e.expenseDate >= :from GROUP BY e.merchant ORDER BY total DESC")
    List<Object[]> getTopMerchants(@Param("u") UUID u, @Param("from") LocalDate from, Pageable p);

    // Monthly trend (last 6 months)
    @Query(value = "SELECT TO_CHAR(expense_date,'YYYY-MM') as month, SUM(amount) as total " +
            "FROM finance_schema.expenses WHERE user_id=:u AND is_deleted=false " +
            "AND expense_date >= :from GROUP BY month ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyTrend(@Param("u") UUID u, @Param("from") LocalDate from);

    // Monthly expense totals for cash flow — native query for date_trunc grouping
    @Query(value = "SELECT TO_CHAR(expense_date,'YYYY-MM') as month, " +
            "COALESCE(SUM(amount),0) as total " +
            "FROM finance_schema.expenses " +
            "WHERE user_id = :u AND is_deleted = false " +
            "AND expense_date >= :from " +
            "GROUP BY month ORDER BY month ASC",
            nativeQuery = true)
    List<Object[]> sumByMonth(@Param("u") UUID u, @Param("from") LocalDate from);
}
