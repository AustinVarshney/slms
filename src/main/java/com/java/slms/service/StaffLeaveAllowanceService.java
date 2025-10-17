package com.java.slms.service;

import com.java.slms.dto.StaffLeaveAllowanceDto;

import java.util.List;

public interface StaffLeaveAllowanceService
{
    void createLeaveAllowanceInCurrentSession(StaffLeaveAllowanceDto dto, Long schoolId);

    void updateLeaveAllowance(Long allowanceId, StaffLeaveAllowanceDto dto, Long schoolId);

    StaffLeaveAllowanceDto getAllowance(Long staffId, Long sessionId, Long schoolId);

    List<StaffLeaveAllowanceDto> getAllowancesForSession(Long sessionId, Long schoolId);

}
