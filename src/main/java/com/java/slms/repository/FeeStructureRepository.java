package com.java.slms.repository;

import com.java.slms.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long>
{
    Optional<FeeStructure> findByClassEntityId(Long classId);

//    List<FeeStructure> findByFeeTypeIgnoreCase(String feeType);

//    List<FeeStructure> findByDueDateBetween(Date startDate, Date endDate);

    boolean existsByClassEntity_Id(Long classId);

    Optional<FeeStructure> findByClassEntity_IdAndSession_Id(Long classId, Long sessionId);

    boolean existsByClassEntity_IdAndSession_Id(Long classId, Long sessionId);


}
