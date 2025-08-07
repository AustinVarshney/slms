package com.java.slms.repository;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>
{
    Optional<Attendance> findByStudentAndDateBetween(Student student, LocalDateTime start, LocalDateTime end);
}
