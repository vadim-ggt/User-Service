package com.innowise.userservice.domain.specification;

import com.innowise.userservice.domain.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

}
