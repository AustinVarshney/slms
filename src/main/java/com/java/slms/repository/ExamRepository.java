package com.java.slms.repository;

import com.java.slms.model.ClassEntity;
import com.java.slms.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long>
{
    Exam findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndClassEntity_ClassNameIgnoreCase(String name, String className);

    List<Exam> findByClassEntity_ClassNameIgnoreCase(String className);

    Exam findByNameIgnoreCaseAndClassEntity(String name, ClassEntity classEntity);

    List<Exam> findByClassEntity_Id(Long classId);

    @Query("SELECT e FROM Exam e WHERE e.id = :examId AND e.classEntity.id = :classId")
    Exam findByIdAndClassEntityId(@Param("examId") Long examId, @Param("classId") Long classId);

    boolean existsByNameIgnoreCaseAndClassEntity_Id(String examName, Long classId);


}
