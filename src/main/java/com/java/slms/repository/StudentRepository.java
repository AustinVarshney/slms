package com.java.slms.repository;

import com.java.slms.model.Student;
import com.java.slms.model.User;
import com.java.slms.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Student s " +
            "JOIN s.currentClass c " +
            "WHERE LOWER(c.className) = LOWER(:className) " +
            "AND LOWER(s.panNumber) = LOWER(:panNumber)")
    boolean existsByClassNameAndPanNumberIgnoreCase(@Param("className") String className,
                                                    @Param("panNumber") String panNumber);


    // Find students who are marked present today
    @Query("SELECT a.student FROM Attendance a WHERE a.present = true AND DATE(a.date) = CURRENT_DATE")
    List<Student> findStudentsPresentToday();

    @Query("SELECT DISTINCT s FROM Student s JOIN s.attendanceRecords a " +
            "WHERE a.present = true AND a.date BETWEEN :start AND :end")
    List<Student> findStudentsPresentToday(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT s FROM Student s JOIN s.attendanceRecords a " +
            "WHERE s.currentClass.id = :classId AND a.present = true AND a.date BETWEEN :start AND :end")
    List<Student> findStudentsPresentTodayByClassId(
            @Param("classId") Long classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Student> findByStatusAndCurrentClass_Id(UserStatus status, Long classId);

    List<Student> findByCurrentClass_Id(Long classId);

    List<Student> findByStatus(UserStatus status);

    Optional<Student> findByPanNumberAndStatus(String panNumber, UserStatus status);

    @Query("SELECT COALESCE(MAX(s.classRollNumber), 0) FROM Student s WHERE s.currentClass.id = :classId")
    Integer findMaxClassRollNumberByCurrentClassId(@Param("classId") Long classId);

    List<Student> findByPanNumberIn(List<String> panNumbers);

    List<Student> findBySession_Id(Long sessionId);

}
