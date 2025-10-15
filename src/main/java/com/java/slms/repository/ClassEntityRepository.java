package com.java.slms.repository;

import com.java.slms.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long>
{
    ClassEntity findByClassNameIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClassEntity c WHERE LOWER(c.className) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    Optional<ClassEntity> findByClassNameIgnoreCaseAndSessionId(String className, Long sessionId);

    Optional<ClassEntity> findByIdAndSessionId(Long classId, Long sessionId);

    Optional<ClassEntity> findByIdAndSession_Active(Long classId, boolean isActive);

    Optional<ClassEntity> findByClassNameAndSession_Active(String className, boolean isActive);

    List<ClassEntity> findBySession_Id(Long sessionId);

    boolean existsByIdAndSessionId(Long classId, Long sessionId);
    
    // Find class where teacher is assigned as class teacher
    Optional<ClassEntity> findByClassTeacher_Id(Long teacherId);
    
    List<ClassEntity> findAllByClassTeacher_Id(Long teacherId);
}
