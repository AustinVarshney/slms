package com.java.slms.repository;

import com.java.slms.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long>
{
    boolean existsBySubjectNameIgnoreCaseAndClassEntity_Id(String subjectName, Long classId);

    List<Subject> findByClassEntity_Id(Long classId);

    @Query("SELECT s FROM Subject s WHERE s.id = :subjectId AND s.school.id = :schoolId")
    Optional<Subject> findSubjectByIdAndSchoolId(@Param("subjectId") Long subjectId, @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Subject s WHERE s.id = :subjectId AND s.school.id = :schoolId AND s.classEntity.id = :classId")
    Optional<Subject> findSubjectByIdAndSchoolIdAndClassId(
            @Param("subjectId") Long subjectId,
            @Param("schoolId") Long schoolId,
            @Param("classId") Long classId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Subject s " +
            "WHERE LOWER(s.subjectName) = LOWER(:subjectName) " +
            "AND s.classEntity.id = :classId " +
            "AND s.school.id = :schoolId")
    boolean existsBySubjectNameAndClassIdAndSchoolId(
            @Param("subjectName") String subjectName,
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Subject s WHERE s.school.id = :schoolId")
    List<Subject> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Subject s WHERE s.id = :subjectId AND s.school.id = :schoolId")
    Optional<Subject> findByIdAndSchoolId(@Param("subjectId") Long subjectId, @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Subject s WHERE s.classEntity.id = :classId AND s.school.id = :schoolId")
    List<Subject> findByClassEntityIdAndSchoolId(@Param("classId") Long classId, @Param("schoolId") Long schoolId);


}