package com.corecompass.auth.repository;

import com.corecompass.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailAndIsDeletedFalse(String email);

    Optional<UserEntity> findByGoogleIdAndIsDeletedFalse(String googleId);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByIdAndIsDeletedFalse(UUID id);
}
