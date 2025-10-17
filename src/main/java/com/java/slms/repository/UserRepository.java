package com.java.slms.repository;

import com.java.slms.model.User;
import com.java.slms.util.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPanNumberIgnoreCase(String panNumber);

    boolean existsByRolesContaining(RoleEnum role);

    List<User> findByEnabled(boolean enabled);

    List<User> findByPanNumberIsNotNullAndEnabledTrue();

    Optional<User> findByPanNumberIgnoreCaseAndEnabledTrue(String panNumber);

    List<User> findByEmailIsNotNullAndEnabledTrue();

    Optional<User> findByEmailIgnoreCaseAndEnabledTrue(String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.student s " +
            "LEFT JOIN FETCH u.admin a " +
            "LEFT JOIN FETCH u.nonTeachingStaff nts " +
            "LEFT JOIN FETCH u.teacher t " +
            "WHERE u.email = :email OR u.panNumber = :email")
    Optional<User> findUserWithRoles(@Param("email") String email);

}