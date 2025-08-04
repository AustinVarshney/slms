package com.java.slms.repository;

import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>
{
    Attendance findByStudentAndDateBetween(Student student, LocalDateTime startDate, LocalDateTime endDate);
}
