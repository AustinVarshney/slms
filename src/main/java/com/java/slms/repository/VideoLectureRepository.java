package com.java.slms.repository;

import com.java.slms.model.VideoLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoLectureRepository extends JpaRepository<VideoLecture, Long> {
    
    // Find all lectures by teacher ID, school ID and session ID
    @Query("SELECT v FROM VideoLecture v WHERE v.teacher.id = :teacherId AND v.school.id = :schoolId AND v.session.id = :sessionId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByTeacherIdAndSchoolIdAndSessionId(@Param("teacherId") Long teacherId, @Param("schoolId") Long schoolId, @Param("sessionId") Long sessionId);
    
    // Find all lectures by teacher ID and school ID (without session filter - for backward compatibility)
    @Query("SELECT v FROM VideoLecture v WHERE v.teacher.id = :teacherId AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByTeacherIdAndSchoolId(@Param("teacherId") Long teacherId, @Param("schoolId") Long schoolId);
    
    // Find lectures by class, section and session
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.school.id = :schoolId AND v.session.id = :sessionId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSchoolIdAndSessionId(
        @Param("className") String className, 
        @Param("section") String section,
        @Param("schoolId") Long schoolId,
        @Param("sessionId") Long sessionId
    );
    
    // Find lectures by class and section (without session - for backward compatibility)
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSchoolId(
        @Param("className") String className, 
        @Param("section") String section,
        @Param("schoolId") Long schoolId
    );
    
    // Find lectures by class, section, subject and session
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.subject = :subject AND v.school.id = :schoolId AND v.session.id = :sessionId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSubjectAndSchoolIdAndSessionId(
        @Param("className") String className, 
        @Param("section") String section, 
        @Param("subject") String subject,
        @Param("schoolId") Long schoolId,
        @Param("sessionId") Long sessionId
    );
    
    // Find lectures by class, section and subject (without session - for backward compatibility)
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.subject = :subject AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSubjectAndSchoolId(
        @Param("className") String className, 
        @Param("section") String section, 
        @Param("subject") String subject,
        @Param("schoolId") Long schoolId
    );
    
    // Find all lectures by school ID and session ID
    @Query("SELECT v FROM VideoLecture v WHERE v.school.id = :schoolId AND v.session.id = :sessionId ORDER BY v.createdAt DESC")
    List<VideoLecture> findBySchoolIdAndSessionIdOrderByCreatedAtDesc(@Param("schoolId") Long schoolId, @Param("sessionId") Long sessionId);
    
    // Find all lectures by school ID (without session - for backward compatibility)
    @Query("SELECT v FROM VideoLecture v WHERE v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findBySchoolIdOrderByCreatedAtDesc(@Param("schoolId") Long schoolId);
}
