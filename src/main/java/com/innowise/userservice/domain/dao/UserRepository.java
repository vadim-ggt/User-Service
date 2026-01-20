package com.innowise.userservice.domain.dao;

import com.innowise.userservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Transactional
    @Query("update User u set u.name = :name," +
            " u.surname = :surname," +
            " u.active = :active where u.id = :id")
    int updateUserById(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("surname") String surname,
            @Param("active") Boolean active
    );

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
