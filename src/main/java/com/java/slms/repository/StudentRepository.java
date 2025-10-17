package com.java.slms.repository;

import com.java.slms.model.Student;
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

    @Query("SELECT DISTINCT s FROM Student s JOIN s.attendanceRecords a " +
            "WHERE s.school.id = :schoolId AND a.present = true AND a.date BETWEEN :start AND :end")
    List<Student> findStudentsPresentToday(
            @Param("schoolId") Long schoolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT s FROM Student s JOIN s.attendanceRecords a " +
            "WHERE s.currentClass.id = :classId AND s.school.id = :schoolId AND a.present = true AND a.date BETWEEN :start AND :end")
    List<Student> findStudentsPresentTodayByClassId(
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(MAX(s.classRollNumber), 0) " +
            "FROM Student s " +
            "WHERE s.currentClass.id = :classId " +
            "AND s.school.id = :schoolId")
    Integer findMaxClassRollNumberByCurrentClassIdAndSchoolId(
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s " +
            "WHERE LOWER(s.panNumber) = LOWER(:panNumber) " +
            "AND s.school.id = :schoolId " +
            "AND s.status = 'ACTIVE'")
    Optional<Student> findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(
            @Param("panNumber") String panNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s " +
            "WHERE LOWER(s.panNumber) = LOWER(:panNumber)")
    Optional<Student> findByPanNumberIgnoreCase(
            @Param("panNumber") String panNumber);


    @Query("SELECT s FROM Student s " +
            "WHERE LOWER(s.panNumber) = LOWER(:panNumber) " +
            "AND s.school.id = :schoolId ")
    Optional<Student> findByPanNumberIgnoreCaseAndSchool_Id(
            @Param("panNumber") String panNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s " +
            "WHERE LOWER(s.panNumber) = LOWER(:panNumber) " +
            "AND s.school.id = :schoolId " +
            "AND s.status = 'INACTIVE'")
    Optional<Student> findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(
            @Param("panNumber") String panNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s " +
            "WHERE LOWER(s.panNumber) = LOWER(:panNumber) " +
            "AND s.school.id = :schoolId")
    Optional<Student> findByPanNumberIgnoreCaseAndSchoolId(
            @Param("panNumber") String panNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s WHERE s.currentClass.id = :currentClassId " +
            "AND s.school.id = :schoolId AND s.status = 'ACTIVE'")
    List<Student> findByCurrentClassIdAndSchoolIdAndStatusActive(
            @Param("currentClassId") Long currentClassId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId")
    List<Student> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId AND s.status = 'ACTIVE'")
    List<Student> findAllActiveStudentsBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId AND s.session.active = true")
    List<Student> findStudentsBySchoolIdAndActiveSession(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Student s " +
            "WHERE s.school.id = :schoolId " +
            "AND LOWER(s.panNumber) IN :panNumbers")
    List<Student> findBySchoolIdAndPanNumberInIgnoreCase(
            @Param("schoolId") Long schoolId,
            @Param("panNumbers") List<String> panNumbers);


}
