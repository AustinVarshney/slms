package com.java.slms.repository;

import com.java.slms.model.ClassEntity;
import com.java.slms.model.ClassExam;
import com.java.slms.model.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassExamRepository extends JpaRepository<ClassExam, Long>
{
    @Query("SELECT CASE WHEN COUNT(ce) > 0 THEN true ELSE false END FROM ClassExam ce " +
            "WHERE ce.classEntity = :classEntity AND ce.examType = :examType AND ce.school.id = :schoolId")
    boolean existsByClassAndExamTypeAndSchoolId(@Param("classEntity") ClassEntity classEntity,
                                                @Param("examType") ExamType examType,
                                                @Param("schoolId") Long schoolId);

    @Query("SELECT ce FROM ClassExam ce " +
            "WHERE ce.classEntity.id = :classId AND ce.school.id = :schoolId")
    List<ClassExam> findByClassIdAndSchoolId(@Param("classId") Long classId,
                                             @Param("schoolId") Long schoolId);

    @Query("SELECT ce FROM ClassExam ce " +
            "WHERE ce.examType = :examType AND ce.school.id = :schoolId")
    List<ClassExam> findByExamTypeAndSchoolId(@Param("examType") ExamType examType,
                                              @Param("schoolId") Long schoolId);

    @Query("SELECT ce FROM ClassExam ce " +
            "WHERE ce.classEntity.id = :classId " +
            "AND ce.examType.id = :examTypeId " +
            "AND ce.school.id = :schoolId")
    Optional<ClassExam> findByClassIdAndExamTypeIdAndSchoolId(@Param("classId") Long classId,
                                                              @Param("examTypeId") Long examTypeId,
                                                              @Param("schoolId") Long schoolId);

    List<ClassExam> findByExamType_Id(Long examTypeId);

    @Query("SELECT ce FROM ClassExam ce " +
            "WHERE ce.classEntity.id = :classId " +
            "AND ce.examType.id = :examTypeId " +
            "AND ce.classEntity.school.id = :schoolId " +
            "AND ce.classEntity.session.active = true")
    Optional<ClassExam> findByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(
            @Param("classId") Long classId,
            @Param("examTypeId") Long examTypeId,
            @Param("schoolId") Long schoolId);

    @Modifying
    @Query("DELETE FROM ClassExam ce WHERE ce.classEntity.id = :classId " +
            "AND ce.examType.id = :examTypeId " +
            "AND ce.classEntity.school.id = :schoolId " +
            "AND ce.classEntity.session.active = true")
    int deleteByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(
            @Param("classId") Long classId,
            @Param("examTypeId") Long examTypeId,
            @Param("schoolId") Long schoolId);

}
