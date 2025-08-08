package com.java.slms.service;

import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.FeeResponseDTO;
import com.java.slms.dto.StudentDto;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;

import java.util.Date;
import java.util.List;

public interface FeeService
{
    FeeResponseDTO createFee(FeeRequestDTO feeRequestDTO);

    List<FeeResponseDTO> getFeesByStudentPan(String panNumber);

    List<FeeResponseDTO> getFeesByStudentPan(String panNumber, FeeMonth month);

    List<FeeResponseDTO> getFeesByFeeStructureId(Long feeStructureId);

    List<FeeResponseDTO> getFeesByFeeStructureId(Long feeStructureId, FeeMonth month);

    List<FeeResponseDTO> getFeesByStatus(FeeStatus status);

    List<FeeResponseDTO> getFeesByStatus(FeeStatus status, FeeMonth month);

    List<FeeResponseDTO> getFeesPaidBetween(Date startDate, Date endDate);

    List<FeeResponseDTO> getFeesByStudentPanAndStatus(String panNumber, FeeStatus status);

    List<FeeResponseDTO> getFeesByStudentPanAndStatus(String panNumber, FeeStatus status, FeeMonth month);

    List<StudentDto> getDefaulters(Long feeStructureId, FeeMonth month);
}
