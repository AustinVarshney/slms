package com.java.slms.repository;

import com.java.slms.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long>
{
    List<FeeStructure> findByClassEntityId(Long classId);

    List<FeeStructure> findByFeeTypeIgnoreCase(String feeType);

    List<FeeStructure> findByDueDateBetween(Date startDate, Date endDate);

    boolean existsByFeeTypeIgnoreCaseAndClassEntity_Id(String feeType, Long classId);

}
