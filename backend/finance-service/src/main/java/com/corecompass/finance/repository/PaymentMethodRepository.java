package com.corecompass.finance.repository;

import com.corecompass.finance.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, UUID> {
    @Query("SELECT p FROM PaymentMethodEntity p WHERE p.isSystem=true OR p.createdBy=:u ORDER BY p.name ASC")
    List<PaymentMethodEntity> findAvailableForUser(@Param("u") UUID u);
}
