package com.corecompass.auth.repository;

import com.corecompass.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalse(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :cutoff OR r.revoked = true")
    void deleteExpiredAndRevoked(Instant cutoff);
}
