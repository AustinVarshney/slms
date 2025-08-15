package com.java.slms.repository;

import com.java.slms.model.User;
import com.java.slms.util.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

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

}