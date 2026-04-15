package com.corecompass.habits.repository;

import com.corecompass.habits.entity.HabitCheckinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitCheckinRepository extends JpaRepository<HabitCheckinEntity, UUID> {
    Optional<HabitCheckinEntity> findByHabitIdAndCheckinDate(UUID habitId, LocalDate date);

    Optional<HabitCheckinEntity> findByHabitIdAndCheckinDateAndIsSkipFalse(UUID habitId, LocalDate date);

    List<HabitCheckinEntity> findByHabitIdOrderByCheckinDateDesc(UUID habitId);

    Page<HabitCheckinEntity> findByHabitIdOrderByCheckinDateDesc(UUID habitId, Pageable p);

    @Query("SELECT COUNT(c) FROM HabitCheckinEntity c WHERE c.userId=:u AND c.checkinDate=:d AND c.isSkip=false")
    long countByUserAndDate(@Param("u") UUID u, @Param("d") LocalDate d);

    @Query("SELECT COUNT(DISTINCT h.id) FROM HabitEntity h WHERE h.userId=:u AND h.status='ACTIVE' AND h.isDeleted=false")
    long countActiveHabits(@Param("u") UUID u);

    @Query("SELECT c FROM HabitCheckinEntity c WHERE c.habitId=:hid AND c.checkinDate BETWEEN :from AND :to ORDER BY c.checkinDate ASC")
    List<HabitCheckinEntity> findByHabitAndDateRange(@Param("hid") UUID hid, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
