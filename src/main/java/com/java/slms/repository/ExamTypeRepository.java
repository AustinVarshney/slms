package com.java.slms.repository;

import com.java.slms.model.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamTypeRepository extends JpaRepository<ExamType, Long>
{
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ExamType e WHERE LOWER(e.name) = LOWER(:name) AND e.school.id = :schoolId")
    boolean existsByNameIgnoreCaseAndSchoolId(@Param("name") String name, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM ExamType e WHERE e.id = :examTypeId AND e.school.id = :schoolId")
    Optional<ExamType> findByIdAndSchoolId(@Param("examTypeId") Long examTypeId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM ExamType e WHERE e.school.id = :schoolId")
    List<ExamType> findAllBySchoolId(@Param("schoolId") Long schoolId);



}
