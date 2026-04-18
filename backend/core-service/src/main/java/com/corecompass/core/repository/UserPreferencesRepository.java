package com.corecompass.core.repository;

import com.corecompass.core.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, UUID> {

    // userId IS the PK so findById works, but alias for clarity
    Optional<UserPreferencesEntity> findByUserId(UUID userId);
}