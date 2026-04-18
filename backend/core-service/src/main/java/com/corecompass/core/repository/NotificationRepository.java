package com.corecompass.core.repository;

import com.corecompass.core.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    // Paginated list — unread first, then by date desc
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :u " +
            "ORDER BY n.isRead ASC, n.createdAt DESC")
    Page<NotificationEntity> findByUserId(@Param("u") UUID u, Pageable pageable);

    // Ownership-safe single fetch
    Optional<NotificationEntity> findByIdAndUserId(UUID id, UUID userId);

    // Mark all read for a user
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :u AND n.isRead = false")
    int markAllRead(@Param("u") UUID u);

    // Unread count (for badge on frontend)
    long countByUserIdAndIsReadFalse(UUID userId);
}