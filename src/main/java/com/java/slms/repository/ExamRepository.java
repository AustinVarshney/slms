package com.java.slms.repository;

import com.java.slms.model.ClassExam;
import com.java.slms.model.Exam;
import com.java.slms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>
{
    @Query("SELECT e FROM Exam e WHERE e.classExam.id = :classExamId AND e.school.id = :schoolId")
    List<Exam> findByClassExamIdAndSchoolId(@Param("classExamId") Long classExamId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Exam e WHERE e.classExam.id = :classExamId AND e.subject.id = :subjectId AND e.school.id = :schoolId")
    Optional<Exam> findByClassExamIdAndSubjectIdAndSchoolId(
            @Param("classExamId") Long classExamId,
            @Param("subjectId") Long subjectId,
            @Param("schoolId") Long schoolId
    );

    @Query("SELECT e FROM Exam e WHERE e.subject.id = :subjectId AND e.school.id = :schoolId")
    List<Exam> findBySubjectIdAndSchoolId(@Param("subjectId") Long subjectId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Exam e WHERE e.classExam.classEntity.id = :classId AND e.school.id = :schoolId")
    List<Exam> findByClassIdAndSchoolId(@Param("classId") Long classId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Exam e WHERE e.school.id = :schoolId")
    List<Exam> findBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Exam e WHERE e.classEntity.id = :classId")
    List<Exam> findByClassEntity_Id(@Param("classId") Long classId);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Exam e WHERE e.classExam = :classExam AND e.subject = :subject AND e.school.id = :schoolId")
    boolean existsByClassExamAndSubjectAndSchoolId(@Param("classExam") ClassExam classExam, @Param("subject") Subject subject, @Param("schoolId") Long schoolId);
}
