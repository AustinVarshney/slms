package com.java.slms.repository;

import com.java.slms.model.VideoLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoLectureRepository extends JpaRepository<VideoLecture, Long> {
    
    // Find all lectures by teacher ID and school ID
    @Query("SELECT v FROM VideoLecture v WHERE v.teacher.id = :teacherId AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByTeacherIdAndSchoolId(@Param("teacherId") Long teacherId, @Param("schoolId") Long schoolId);
    
    // Find lectures by class and section
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSchoolId(
        @Param("className") String className, 
        @Param("section") String section,
        @Param("schoolId") Long schoolId
    );
    
    // Find lectures by class, section and subject
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.subject = :subject AND v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSubjectAndSchoolId(
        @Param("className") String className, 
        @Param("section") String section, 
        @Param("subject") String subject,
        @Param("schoolId") Long schoolId
    );
    
    // Find all lectures by school ID
    @Query("SELECT v FROM VideoLecture v WHERE v.school.id = :schoolId ORDER BY v.createdAt DESC")
    List<VideoLecture> findBySchoolIdOrderByCreatedAtDesc(@Param("schoolId") Long schoolId);
}
