package com.java.slms.repository;

import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.model.TeacherQuery;
import com.java.slms.util.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherQueryRepository extends JpaRepository<TeacherQuery, Long>
{
    // Query methods with session filtering
    @Query("SELECT q FROM TeacherQuery q WHERE q.teacher = :teacher AND q.status = :status AND q.school.id = :schoolId AND q.session.id = :sessionId")
    List<TeacherQuery> findByTeacherAndStatusAndSchoolIdAndSessionId(@Param("teacher") Teacher teacher,
                                                         @Param("status") QueryStatus status,
                                                         @Param("schoolId") Long schoolId,
                                                         @Param("sessionId") Long sessionId);

    @Query("SELECT q FROM TeacherQuery q WHERE q.teacher = :teacher AND q.school.id = :schoolId AND q.session.id = :sessionId")
    List<TeacherQuery> findByTeacherAndSchoolIdAndSessionId(@Param("teacher") Teacher teacher,
                                                @Param("schoolId") Long schoolId,
                                                @Param("sessionId") Long sessionId);

    @Query("SELECT tq FROM TeacherQuery tq WHERE tq.admin = :admin AND tq.status = :status AND tq.teacher.school.id = :schoolId AND tq.session.id = :sessionId")
    List<TeacherQuery> findByAdminAndStatusAndSchoolIdAndSessionId(@Param("admin") Admin admin, 
                                                                   @Param("status") QueryStatus status, 
                                                                   @Param("schoolId") Long schoolId,
                                                                   @Param("sessionId") Long sessionId);

    @Query("SELECT tq FROM TeacherQuery tq WHERE tq.admin = :admin AND tq.teacher.school.id = :schoolId AND tq.session.id = :sessionId")
    List<TeacherQuery> findByAdminAndSchoolIdAndSessionId(@Param("admin") Admin admin, 
                                                          @Param("schoolId") Long schoolId,
                                                          @Param("sessionId") Long sessionId);

    // Backward compatibility - without session filtering
    List<TeacherQuery> findByTeacher(Teacher teacher);

    List<TeacherQuery> findByTeacherAndStatus(Teacher teacher, QueryStatus status);

    List<TeacherQuery> findByAdmin(Admin admin);

    List<TeacherQuery> findByAdminAndStatus(Admin admin, QueryStatus status);

    @Query("SELECT q FROM TeacherQuery q WHERE q.teacher = :teacher AND q.status = :status AND q.school.id = :schoolId")
    List<TeacherQuery> findByTeacherAndStatusAndSchoolId(@Param("teacher") Teacher teacher,
                                                         @Param("status") QueryStatus status,
                                                         @Param("schoolId") Long schoolId);

    @Query("SELECT q FROM TeacherQuery q WHERE q.teacher = :teacher AND q.school.id = :schoolId")
    List<TeacherQuery> findByTeacherAndSchoolId(@Param("teacher") Teacher teacher,
                                                @Param("schoolId") Long schoolId);

    @Query("SELECT tq FROM TeacherQuery tq WHERE tq.id = :id AND tq.school.id = :schoolId")
    Optional<TeacherQuery> findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId);

    @Query("SELECT tq FROM TeacherQuery tq WHERE tq.admin = :admin AND tq.status = :status AND tq.teacher.school.id = :schoolId")
    List<TeacherQuery> findByAdminAndStatusAndSchoolId(@Param("admin") Admin admin, @Param("status") QueryStatus status, @Param("schoolId") Long schoolId);

    @Query("SELECT tq FROM TeacherQuery tq WHERE tq.admin = :admin AND tq.teacher.school.id = :schoolId")
    List<TeacherQuery> findByAdminAndSchoolId(@Param("admin") Admin admin, @Param("schoolId") Long schoolId);


}
