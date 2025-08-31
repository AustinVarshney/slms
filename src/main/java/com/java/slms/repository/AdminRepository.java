package com.java.slms.repository;

import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long>
{
    boolean existsByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    Optional<Admin> findByEmailIgnoreCase(String email);

    Optional<Admin> findByEmailIgnoreCaseAndStatus(String email, UserStatus status);

    List<Admin> findByEmailIn(List<String> emails);

    List<Admin> findByStatus(UserStatus userStatus);
}
