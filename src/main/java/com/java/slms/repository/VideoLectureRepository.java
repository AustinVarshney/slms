package com.java.slms.repository;

import com.java.slms.entity.VideoLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoLectureRepository extends JpaRepository<VideoLecture, Long> {
    
    // Find all lectures by teacher ID
    List<VideoLecture> findByTeacherIdAndIsActiveTrue(Long teacherId);
    
    // Find lectures by class and section
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.isActive = true ORDER BY v.uploadedAt DESC")
    List<VideoLecture> findByClassNameAndSection(@Param("className") String className, @Param("section") String section);
    
    // Find lectures by class, section and subject
    @Query("SELECT v FROM VideoLecture v WHERE v.className = :className AND v.section = :section AND v.subject = :subject AND v.isActive = true ORDER BY v.uploadedAt DESC")
    List<VideoLecture> findByClassNameAndSectionAndSubject(
        @Param("className") String className, 
        @Param("section") String section, 
        @Param("subject") String subject
    );
    
    // Find all active lectures
    List<VideoLecture> findByIsActiveTrueOrderByUploadedAtDesc();
}
