package com.java.slms.service;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.FeeResponseDTO;
import com.java.slms.dto.StudentRequestDto;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface FeeService
{
    void payFeesOfStudent(FeeRequestDTO feeRequestDTO);

    List<FeeCatalogDto> getAllFeeCatalogs();

    FeeCatalogDto getFeeCatalogByStudentPanNumber(String panNumber);

    //    @Scheduled(cron = "0 0 0 15 * ?")  // Runs at midnight on the 15th day of every month
        @Transactional
        void markPendingFeesAsOverdue();
}
