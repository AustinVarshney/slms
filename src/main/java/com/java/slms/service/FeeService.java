package com.java.slms.service;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.FeeResponseDTO;
import com.java.slms.dto.StudentRequestDto;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;

import java.util.Date;
import java.util.List;

public interface FeeService
{
    void payFeesOfStudent(FeeRequestDTO feeRequestDTO);

    List<FeeCatalogDto> getAllFeeCatalogs();
}
