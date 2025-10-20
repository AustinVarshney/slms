package com.java.slms.repository;

import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>
{
    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student = :student " +
            "AND a.date BETWEEN :start AND :end " +
            "AND a.school.id = :schoolId")
    Optional<Attendance> findByStudentAndDateBetweenAndSchoolId(
            @Param("student") Student student,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("schoolId") Long schoolId);

    // With month filtering
    @Query("SELECT a FROM Attendance a " +
            "WHERE LOWER(a.student.panNumber) = LOWER(:panNumber) " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "AND FUNCTION('MONTH', a.date) = :monthNumber " +
            "AND a.student.school.id = :schoolId")
    List<Attendance> findByPanNumberAndSessionIdAndMonthWithinSessionAndSchoolId(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd,
            @Param("monthNumber") int monthNumber,
            @Param("schoolId") Long schoolId);

    // Without month filtering
    @Query("SELECT a FROM Attendance a " +
            "WHERE LOWER(a.student.panNumber) = LOWER(:panNumber) " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "AND a.student.school.id = :schoolId")
    List<Attendance> findByPanNumberAndSessionIdWithinSessionAndSchoolId(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd,
            @Param("schoolId") Long schoolId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.currentClass.id = :classId " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "AND FUNCTION('MONTH', a.date) = :monthNumber " +
            "AND a.student.school.id = :schoolId")
    List<Attendance> findByClassIdAndSessionIdAndMonthWithinSessionAndSchoolId(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd,
            @Param("monthNumber") int monthNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.currentClass.id = :classId " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "AND a.student.school.id = :schoolId")
    List<Attendance> findByClassIdAndSessionIdWithinSessionAndSchoolId(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd,
            @Param("schoolId") Long schoolId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE LOWER(a.student.panNumber) = LOWER(:panNumber) " +
            "AND a.session.id = :sessionId")
    List<Attendance> findByStudentPanNumberAndSessionId(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId);

}
