package com.java.slms.repository;

import com.java.slms.model.NonTeachingStaff;
import com.java.slms.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NonTeachingStaffRepository extends JpaRepository<NonTeachingStaff, Long>
{
    boolean existsByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    List<NonTeachingStaff> findByStatus(UserStatus userStatus);

    Optional<NonTeachingStaff> findByEmailIgnoreCase(String email);

    List<NonTeachingStaff> findByEmailIn(List<String> emails);
}
