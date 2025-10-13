package com.java.slms.service;

import com.java.slms.dto.LeaveActionRequest;
import com.java.slms.dto.StudentLeaveRequestDTO;
import com.java.slms.dto.StudentLeaveResponse;
import com.java.slms.model.StudentLeaveRecord;
import com.java.slms.util.LeaveStatus;

import java.util.List;

public interface StudentLeaveService
{
    void createLeaveRequest(StudentLeaveRequestDTO dto);

    List<StudentLeaveResponse> getLeavesForLoggedInStudent(String panNumber);

    void takeActionOnLeave(Long leaveId, Long teacherId, LeaveActionRequest request);

    List<StudentLeaveResponse> getLeavesForTeacher(Long teacherId, LeaveStatus status);

}
