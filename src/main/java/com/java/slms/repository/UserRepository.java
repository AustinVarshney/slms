package com.java.slms.repository;

import com.java.slms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPanNumberIgnoreCase(String panNumber);
}
