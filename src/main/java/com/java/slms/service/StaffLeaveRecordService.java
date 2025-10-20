package com.java.slms.service;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.model.Admin;
import com.java.slms.util.LeaveStatus;

import java.util.List;

public interface StaffLeaveRecordService
{
    void raiseLeaveRequest(StaffLeaveRequestDto dto, Long schoolId, String email);

    List<StaffLeaveResponseDto> getMyLeaves(String email, LeaveStatus status, Long schoolId);

    List<StaffLeaveResponseDto> getAllLeavesForAdmin(LeaveStatus status, Long sessionId, Long staffId, Long schoolId);

    void updateLeaveStatus(Long leaveId, Admin admin, StaffLeaveStatusUpdateDto dto, Long schoolId);
}

