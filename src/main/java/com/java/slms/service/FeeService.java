package com.java.slms.service;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FeeService
{
    void payFeesOfStudent(FeeRequestDTO requestDto, Long schoolId);

    List<FeeCatalogDto> getAllFeeCatalogsInActiveSession(Long schoolId);

    FeeCatalogDto getFeeCatalogByStudentPanNumber(String panNumber, Long schoolId);

    void generateFeesForStudent(String panNumber);

    //    @Scheduled(cron = "0 0 0 15 * ?")  // Runs at midnight on the 15th day of every month
    @Transactional
    void markPendingFeesAsOverdue(Long schoolid);
}