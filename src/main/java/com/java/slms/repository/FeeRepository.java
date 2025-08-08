package com.java.slms.repository;

import com.java.slms.model.Fee;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long>
{

    // Existing
    List<Fee> findByStudent_PanNumber(String panNumber);

    List<Fee> findByFeeStructure_Id(Long feeStructureId);

    List<Fee> findByStatus(FeeStatus status);

    List<Fee> findByPaidOnBetween(Date start, Date end);

    List<Fee> findByStudent_PanNumberAndStatus(String panNumber, FeeStatus status);

    List<Fee> findByStudent_PanNumberAndMonth(String panNumber, FeeMonth month);

    List<Fee> findByFeeStructure_IdAndMonth(Long feeStructureId, FeeMonth month);

    List<Fee> findByMonthAndStatus(FeeMonth month, FeeStatus status);

    List<Fee> findByStudent_PanNumberAndStatusAndMonth(String panNumber, FeeStatus status, FeeMonth month);

    boolean existsByFeeStructureIdAndStudent_PanNumberAndMonth(Long feeStructureId, String panNumber, FeeMonth month);

}
