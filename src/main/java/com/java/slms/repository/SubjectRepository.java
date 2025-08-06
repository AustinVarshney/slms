package com.java.slms.repository;

import com.java.slms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>
{
    boolean existsBySubjectNameIgnoreCase(String subjectName);

    Subject findBySubjectNameIgnoreCase(String subjectName);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Subject s WHERE LOWER(s.subjectName) = LOWER(:subjectName) " +
            "AND LOWER(s.classEntity.className) = LOWER(:className)")
    boolean existsBySubjectNameAndClassNameIgnoreCase(@Param("subjectName") String subjectName,
                                                      @Param("className") String className);

    @Query("SELECT s FROM Subject s WHERE LOWER(s.subjectName) = LOWER(:subjectName) " +
            "AND LOWER(s.classEntity.className) = LOWER(:className)")
    Subject findBySubjectNameIgnoreCaseAndClassNameIgnoreCase(@Param("subjectName") String subjectName,
                                                              @Param("className") String className);

    boolean existsBySubjectNameIgnoreCaseAndClassEntity_Id(String subjectName, Long classId);

    List<Subject> findByClassEntity_Id(Long classId);

}