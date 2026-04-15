package com.corecompass.report.repository;
import com.corecompass.report.entity.WeeklyReportEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReportEntity, UUID> {
    Page<WeeklyReportEntity> findByUserIdOrderByWeekStartDesc(UUID userId, Pageable p);
    Optional<WeeklyReportEntity> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
    boolean existsByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
