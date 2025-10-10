package com.java.slms.repository;

import com.java.slms.model.Session;
import com.java.slms.model.StaffLeaveAllowance;
import com.java.slms.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffLeaveAllowanceRepository extends JpaRepository<StaffLeaveAllowance, Long>
{
    Optional<StaffLeaveAllowance> findByTeacherAndSession(Teacher teacher, Session session);

}
