package com.java.slms.repository;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>
{
    Optional<Attendance> findByStudentAndDateBetween(Student student, LocalDateTime start, LocalDateTime end);

    // With month filtering
    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.panNumber = :panNumber " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "AND FUNCTION('MONTH', a.date) = :month " +
            "ORDER BY a.date DESC")
    List<Attendance> findByStudent_PanNumberAndSession_IdAndMonthWithinSession(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd,
            @Param("month") int month);

    // Without month filtering
    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.panNumber = :panNumber " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStart AND :sessionEnd " +
            "ORDER BY a.date DESC")
    List<Attendance> findByStudent_PanNumberAndSession_IdWithinSession(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId,
            @Param("sessionStart") LocalDateTime sessionStart,
            @Param("sessionEnd") LocalDateTime sessionEnd);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.currentClass.id = :classId " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStartDate AND :sessionEndDate " +
            "AND FUNCTION('MONTH', a.date) = :month " +
            "ORDER BY a.date DESC")
    List<Attendance> findByClassIdAndSessionIdAndMonthWithinSession(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("sessionStartDate") LocalDateTime sessionStartDate,
            @Param("sessionEndDate") LocalDateTime sessionEndDate,
            @Param("month") Integer month);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.currentClass.id = :classId " +
            "AND a.session.id = :sessionId " +
            "AND a.date BETWEEN :sessionStartDate AND :sessionEndDate " +
            "ORDER BY a.date DESC")
    List<Attendance> findByClassIdAndSessionIdWithinSession(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("sessionStartDate") LocalDateTime sessionStartDate,
            @Param("sessionEndDate") LocalDateTime sessionEndDate);


}
