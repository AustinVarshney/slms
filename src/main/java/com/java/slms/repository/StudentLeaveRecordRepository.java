package com.java.slms.repository;

import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.model.StudentLeaveRecord;
import com.java.slms.model.Teacher;
import com.java.slms.util.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentLeaveRecordRepository extends JpaRepository<StudentLeaveRecord, Long>
{
    List<StudentLeaveRecord> findByStudentAndSessionOrderByStartDateDesc(Student student, Session session);

    boolean existsByStudentAndSessionAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Student student,
            Session session,
            LocalDate endDate,
            LocalDate startDate
    );

    List<StudentLeaveRecord> findBySessionAndTeacherOrderByCreatedAtDesc(Session session, Teacher teacher);

    List<StudentLeaveRecord> findBySessionAndTeacherAndStatusOrderByCreatedAtDesc(Session session, Teacher teacher, LeaveStatus status);


}