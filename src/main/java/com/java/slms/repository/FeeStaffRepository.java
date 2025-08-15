package com.java.slms.repository;

import com.java.slms.model.Admin;
import com.java.slms.model.FeeStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeStaffRepository extends JpaRepository<FeeStaff, Long>
{
    boolean existsByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);

    Optional<FeeStaff> findByEmailIgnoreCase(String email);

    List<FeeStaff> findByEmailIn(List<String> emails);
}
