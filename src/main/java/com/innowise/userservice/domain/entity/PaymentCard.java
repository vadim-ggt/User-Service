package com.innowise.userservice.domain.entity;

import com.innowise.userservice.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "payment_cards")
public class PaymentCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 255)
    private String number;

    @Column(nullable = false, length = 255)
    private String holder;

    @Column(name = "expiration_date", nullable = false, length = 7)
    private String expirationDate;

    @Column(nullable = false)
    private Boolean active = true;
}
