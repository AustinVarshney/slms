package com.java.slms.repository;

import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.model.StudentLeaveRecord;
import com.java.slms.model.Teacher;
import com.java.slms.util.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentLeaveRecordRepository extends JpaRepository<StudentLeaveRecord, Long>
{
    List<StudentLeaveRecord> findByStudentAndSessionOrderByStartDateDesc(Student student, Session session);

    boolean existsByStudentAndSessionAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStudent_School_Id(
            Student student,
            Session session,
            LocalDate endDate,
            LocalDate startDate,
            Long schoolId
    );

    @Query("SELECT s FROM StudentLeaveRecord s " +
            "WHERE s.id = :leaveId " +
            "AND s.school.id = :schoolId")
    Optional<StudentLeaveRecord> findByIdAndStudent_School_Id(
            @Param("leaveId") Long leaveId,
            @Param("schoolId") Long schoolId
    );

    List<StudentLeaveRecord> findBySessionAndTeacherOrderByCreatedAtDesc(Session session, Teacher teacher);

    List<StudentLeaveRecord> findBySessionAndTeacherAndStatusOrderByCreatedAtDesc(Session session, Teacher teacher, LeaveStatus status);

    @Query("SELECT s FROM StudentLeaveRecord s " +
            "WHERE s.session = :session " +
            "AND s.teacher = :teacher " +
            "AND s.school.id = :schoolId " +
            "ORDER BY s.createdAt DESC")
    List<StudentLeaveRecord> findBySessionAndTeacherAndStudent_School_IdOrderByCreatedAtDesc(
            @Param("session") Session session,
            @Param("teacher") Teacher teacher,
            @Param("schoolId") Long schoolId);


    @Query("SELECT s FROM StudentLeaveRecord s " +
            "WHERE s.session = :session " +
            "AND s.teacher = :teacher " +
            "AND s.status = :status " +
            "AND s.school.id = :schoolId " +
            "ORDER BY s.createdAt DESC")
    List<StudentLeaveRecord> findBySessionAndTeacherAndStatusAndStudent_School_IdOrderByCreatedAtDesc(
            @Param("session") Session session,
            @Param("teacher") Teacher teacher,
            @Param("status") LeaveStatus status,
            @Param("schoolId") Long schoolId);

}