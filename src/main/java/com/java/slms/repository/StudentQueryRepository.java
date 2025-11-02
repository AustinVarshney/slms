package com.java.slms.repository;

import com.java.slms.model.Student;
import com.java.slms.model.StudentQuery;
import com.java.slms.model.Teacher;
import com.java.slms.util.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentQueryRepository extends JpaRepository<StudentQuery, Long>
{
    // Query methods with session filtering
    @Query("SELECT sq FROM StudentQuery sq WHERE sq.student = :student AND sq.status = :status AND sq.school.id = :schoolId AND sq.session.id = :sessionId")
    List<StudentQuery> findByStudentAndStatusAndSchoolIdAndSessionId(
            @Param("student") Student student,
            @Param("status") QueryStatus status,
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.student = :student AND sq.school.id = :schoolId AND sq.session.id = :sessionId")
    List<StudentQuery> findByStudentAndSchoolIdAndSessionId(
            @Param("student") Student student,
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.teacher = :teacher AND sq.status = :status AND sq.school.id = :schoolId AND sq.session.id = :sessionId")
    List<StudentQuery> findByTeacherAndStatusAndSchoolIdAndSessionId(
            @Param("teacher") Teacher teacher,
            @Param("status") QueryStatus status,
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.teacher = :teacher AND sq.school.id = :schoolId AND sq.session.id = :sessionId")
    List<StudentQuery> findByTeacherAndSchoolIdAndSessionId(
            @Param("teacher") Teacher teacher,
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId);

    // Backward compatibility - without session filtering
    @Query("SELECT sq FROM StudentQuery sq WHERE sq.student = :student AND sq.status = :status AND sq.school.id = :schoolId")
    List<StudentQuery> findByStudentAndStatusAndSchoolId(
            @Param("student") Student student,
            @Param("status") QueryStatus status,
            @Param("schoolId") Long schoolId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.student = :student AND sq.school.id = :schoolId")
    List<StudentQuery> findByStudentAndSchoolId(
            @Param("student") Student student,
            @Param("schoolId") Long schoolId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.id = :queryId AND sq.school.id = :schoolId")
    Optional<StudentQuery> findByIdAndSchoolId(@Param("queryId") Long queryId, @Param("schoolId") Long schoolId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.id = :queryId AND sq.school.id = :schoolId AND sq.session.id = :sessionId")
    Optional<StudentQuery> findByIdAndSchoolIdAndSessionId(@Param("queryId") Long queryId, @Param("schoolId") Long schoolId, @Param("sessionId") Long sessionId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.teacher = :teacher AND sq.status = :status AND sq.school.id = :schoolId")
    List<StudentQuery> findByTeacherAndStatusAndSchoolId(
            @Param("teacher") Teacher teacher,
            @Param("status") QueryStatus status,
            @Param("schoolId") Long schoolId);

    @Query("SELECT sq FROM StudentQuery sq WHERE sq.teacher = :teacher AND sq.school.id = :schoolId")
    List<StudentQuery> findByTeacherAndSchoolId(
            @Param("teacher") Teacher teacher,
            @Param("schoolId") Long schoolId);


}
