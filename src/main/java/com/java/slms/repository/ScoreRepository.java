package com.java.slms.repository;

import com.java.slms.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long>
{
    @Query("SELECT s FROM Score s WHERE s.student.panNumber = :panNumber")
    List<Score> findByStudentPanNumber(@Param("panNumber") String panNumber);

    @Query("SELECT s FROM Score s " + "JOIN s.exam e " + "JOIN s.student st " + "WHERE LOWER(e.name) = LOWER(:examName) " + "AND LOWER(st.currentClass.className) = LOWER(:className)")
    List<Score> findByExamNameAndClassName(@Param("examName") String examName, @Param("className") String className);

    @Query("SELECT s FROM Score s WHERE s.student.panNumber = :panNumber AND s.exam.name = :examName")
    List<Score> findByStudentPanAndExamName(@Param("panNumber") String panNumber, @Param("examName") String examName);

    @Query("SELECT s FROM Score s WHERE s.student.panNumber = :panNumber AND s.subject.subjectName = :subjectName")
    List<Score> findByStudentPanAndSubjectName(@Param("panNumber") String panNumber, @Param("subjectName") String subjectName);

    @Query("SELECT s FROM Score s WHERE s.exam.name = :examName AND s.subject.subjectName = :subjectName AND s.exam.classEntity.className = :className")
    List<Score> findByExamNameAndSubjectNameAndClassName(@Param("examName") String examName, @Param("subjectName") String subjectName, @Param("className") String className);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " + "FROM Score s " + "JOIN s.student st " + "JOIN s.subject sub " + "JOIN s.exam e " + "WHERE LOWER(st.panNumber) = LOWER(:panNumber) " + "AND LOWER(sub.subjectName) = LOWER(:subjectName) " + "AND LOWER(e.name) = LOWER(:examName)")
    boolean existsByStudentPanNumberAndSubjectNameAndExamName(@Param("panNumber") String panNumber, @Param("subjectName") String subjectName, @Param("examName") String examName);

    boolean existsByStudentPanNumberAndSubjectIdAndExamId(String studentPanNumber, Long subjectId, Long examId);

    // Fetch scores by exam ID and class ID
    List<Score> findByExam_IdAndStudent_CurrentClass_Id(Long examId, Long classId);

    @Query("SELECT s FROM Score s " + "JOIN s.student st " + "JOIN s.subject sub " + "JOIN sub.classEntity c " + "JOIN s.exam e " + "WHERE st.panNumber = :panNumber " + "AND sub.id = :subjectId " + "AND e.id = :examId " + "AND c.id = :classId")
    Optional<Score> findByStudentPanNumberAndClassIdAndSubjectIdAndExamId(@Param("panNumber") String panNumber, @Param("classId") Long classId, @Param("subjectId") Long subjectId, @Param("examId") Long examId);
    
    // Additional methods for results management
    @Query("SELECT s FROM Score s WHERE s.student = :student AND s.exam = :exam AND s.subject = :subject")
    Optional<Score> findByStudentAndExamAndSubject(@Param("student") com.java.slms.model.Student student, @Param("exam") com.java.slms.model.Exam exam, @Param("subject") com.java.slms.model.Subject subject);
    
    @Query("SELECT s FROM Score s WHERE s.student = :student AND s.exam = :exam")
    List<Score> findByStudentAndExam(@Param("student") com.java.slms.model.Student student, @Param("exam") com.java.slms.model.Exam exam);
    
    @Query("SELECT s FROM Score s WHERE s.exam = :exam AND s.subject = :subject")
    List<Score> findByExamAndSubject(@Param("exam") com.java.slms.model.Exam exam, @Param("subject") com.java.slms.model.Subject subject);
    
    @Query("SELECT s FROM Score s WHERE s.student = :student AND s.subject = :subject AND s.exam IS NULL")
    Optional<Score> findByStudentAndSubjectAndExamIsNull(@Param("student") com.java.slms.model.Student student, @Param("subject") com.java.slms.model.Subject subject);

}
