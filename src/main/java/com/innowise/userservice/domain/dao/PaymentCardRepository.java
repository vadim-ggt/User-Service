package com.innowise.userservice.domain.dao;

import com.innowise.userservice.domain.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentCardRepository extends
        JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    List<PaymentCard> findByUserId(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentCard c SET c.number = :number, c.holder = :holder, " +
            "c.expirationDate = :expDate, c.active = :active WHERE c.id = :id")
    int updateCardById(
            @Param("id") Long id,
            @Param("number") String number,
            @Param("holder") String holder,
            @Param("expDate") String expDate,
            @Param("active") Boolean active
    );

    @Modifying
    @Transactional
    @Query("UPDATE PaymentCard c SET c.active = :active WHERE c.id = :id")
    int setCardActiveStatus(
            @Param("id") Long id,
            @Param("active") Boolean active
    );
}
