package com.java.slms.repository;

import com.java.slms.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long>
{
    Exam findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Exam> findByClassEntity_ClassNameIgnoreCase(String className);
}
