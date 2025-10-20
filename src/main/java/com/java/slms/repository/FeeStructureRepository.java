package com.java.slms.repository;

import com.java.slms.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long>
{
    Optional<FeeStructure> findByClassEntityId(Long classId);

//    List<FeeStructure> findByFeeTypeIgnoreCase(String feeType);

//    List<FeeStructure> findByDueDateBetween(Date startDate, Date endDate);

    boolean existsByClassEntity_Id(Long classId);

    Optional<FeeStructure> findByClassEntity_IdAndSession_Id(Long classId, Long sessionId);

    @Query("SELECT f FROM FeeStructure f " +
            "WHERE f.classEntity.id = :classId " +
            "AND f.session.id = :sessionId " +
            "AND f.school.id = :schoolId")
    Optional<FeeStructure> findByClassEntity_IdAndSession_IdAndSchool_Id(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT f FROM FeeStructure f " +
            "WHERE f.classEntity.id = :classId " +
            "AND f.school.id = :schoolId")
    Optional<FeeStructure> findByClassIdAndSchoolId(
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId);



    boolean existsByClassEntity_IdAndSession_Id(Long classId, Long sessionId);


}
