package com.corecompass.auth.repository;

import com.corecompass.auth.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    // Find an active (not used, not expired) token for this email
    Optional<PasswordResetTokenEntity> findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, Instant now
    );

    // Find by reset token hash (after OTP verified — step 3)
    Optional<PasswordResetTokenEntity> findByResetTokenHashAndVerifiedTrueAndUsedFalseAndExpiresAtAfter(
            String resetTokenHash, Instant now
    );

    // Cleanup cron — delete old used/expired tokens
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :cutoff OR t.used = true")
    void deleteExpiredOrUsed(Instant cutoff);
}