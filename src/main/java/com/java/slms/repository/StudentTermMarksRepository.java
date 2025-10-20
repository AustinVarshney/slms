package com.java.slms.repository;

import com.java.slms.dto.StudentExamSummaryDto;
import com.java.slms.model.ClassExam;
import com.java.slms.model.StudentEnrollments;
import com.java.slms.model.StudentTermMarks;
import com.java.slms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentTermMarksRepository extends JpaRepository<StudentTermMarks, Long>
{
    @Query("SELECT CASE WHEN COUNT(stm) > 0 THEN true ELSE false END " +
            "FROM StudentTermMarks stm " +
            "WHERE stm.enrollment = :enrollment " +
            "AND stm.classExam = :classExam " +
            "AND stm.subject = :subject")
    boolean existsByEnrollmentAndClassExamAndSubject(@Param("enrollment") StudentEnrollments enrollment,
                                                     @Param("classExam") ClassExam classExam,
                                                     @Param("subject") Subject subject);

    @Query("SELECT stm FROM StudentTermMarks stm " +
            "WHERE LOWER(stm.enrollment.student.panNumber) = LOWER(:panNumber)")
    List<StudentTermMarks> findByPanNumber(@Param("panNumber") String panNumber);

    @Query("SELECT DISTINCT new com.java.slms.dto.StudentExamSummaryDto(" +
            "stm.enrollment.classEntity.className, " +
            "stm.classExam.examType.name, " +
            "stm.classExam.examDate) " +
            "FROM StudentTermMarks stm " +
            "WHERE LOWER(stm.enrollment.student.panNumber) = LOWER(:panNumber)")
    List<StudentExamSummaryDto> findExamSummaryByPanNumber(@Param("panNumber") String panNumber);


}
