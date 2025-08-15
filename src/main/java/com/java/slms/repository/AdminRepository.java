package com.java.slms.repository;

import com.java.slms.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long>
{
    boolean existsByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}
