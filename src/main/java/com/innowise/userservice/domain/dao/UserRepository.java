package com.innowise.userservice.domain.dao;

import com.innowise.userservice.domain.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;


import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"cards"})
    @NonNull
    Optional<User> findById(@NonNull Long id);

    @Modifying
    @Transactional
    @Query("update User u set u.active = :active where u.id = :id")
    int setUserActiveStatus(
            @Param("id") Long id,
            @Param("active") Boolean active
    );

    @Query(value =
            "SELECT * FROM users u " +
                    "WHERE u.surname LIKE :letter || '%'",
            nativeQuery = true)
    Page<User> findBySurnameStartsWith(@Param("letter") String letter, Pageable pageable);

}

