package com.java.slms.repository;

import com.java.slms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>
{
    boolean existsBySubjectNameIgnoreCase(String subjectName);

    Subject findBySubjectNameIgnoreCase(String subjectName);
}