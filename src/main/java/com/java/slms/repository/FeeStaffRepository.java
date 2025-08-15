package com.java.slms.repository;

import com.java.slms.model.FeeStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeStaffRepository extends JpaRepository<FeeStaff, Long>
{
    boolean existsByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}
