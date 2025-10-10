package com.java.slms.service;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.model.Admin;
import com.java.slms.model.StaffLeaveRecord;
import com.java.slms.util.LeaveStatus;

import java.util.List;

public interface StaffLeaveService
{
    void raiseLeaveRequest(StaffLeaveRequestDto dto);

    List<StaffLeaveResponseDto> getMyLeaves(Long teacherId, LeaveStatus status);

    List<StaffLeaveResponseDto> getAllLeavesForAdmin(LeaveStatus status, Long sessionId, Long teacherId);

    void updateLeaveStatus(Long leaveId, Admin admin, StaffLeaveStatusUpdateDto dto);

}
