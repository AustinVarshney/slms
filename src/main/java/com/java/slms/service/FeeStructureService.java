package com.java.slms.service;

import com.java.slms.dto.FeeStructureRequestDTO;
import com.java.slms.dto.FeeStructureResponseDTO;

import java.util.Date;
import java.util.List;

public interface FeeStructureService
{
    FeeStructureResponseDTO createFeeStructure(FeeStructureRequestDTO dto);

    FeeStructureResponseDTO updateFeeStructure(Long id, FeeStructureRequestDTO dto);

    FeeStructureResponseDTO getFeeStructureById(Long id);

    List<FeeStructureResponseDTO> getFeeStructuresByClassId(Long classId);

    List<FeeStructureResponseDTO> getFeeStructuresByFeeType(String feeType);

    List<FeeStructureResponseDTO> getFeeStructuresByDueDateRange(Date startDate, Date endDate);

    void deleteFeeStructure(Long id);
}

