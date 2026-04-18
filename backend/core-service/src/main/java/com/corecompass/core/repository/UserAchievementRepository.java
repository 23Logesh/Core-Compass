package com.corecompass.core.repository;

import com.corecompass.core.entity.UserAchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity, UUID> {

    List<UserAchievementEntity> findByUserIdOrderByEarnedAtDesc(UUID userId);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
}