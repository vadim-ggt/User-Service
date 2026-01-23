package com.innowise.userservice.domain.dao;

import com.innowise.userservice.domain.entity.PaymentCard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentCardRepository extends
        JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    PaymentCard findPaymentCardById(Long id);

    Page<PaymentCard> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentCard c SET c.active = :active WHERE c.id = :id")
    int setCardActiveStatus(
            @Param("id") Long id,
            @Param("active") Boolean active
    );

    boolean existsByNumber(@NotBlank(message = "Card number is required")
                           @Size(max = 255, message = "Card number cannot be longer than 255 characters")
                           String number);
}