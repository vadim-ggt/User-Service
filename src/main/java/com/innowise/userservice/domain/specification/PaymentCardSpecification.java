package com.innowise.userservice.domain.specification;

import com.innowise.userservice.domain.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentCardSpecification {

    private PaymentCardSpecification() {}


    public static Specification<PaymentCard> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<PaymentCard> hasUserName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("user").get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<PaymentCard> hasUserSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("user").get("surname")),
                    "%" + surname.toLowerCase() + "%");
        };
    }

    public static Specification<PaymentCard> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }
}
