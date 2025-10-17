package com.java.slms.service;

import com.java.slms.dto.LeaveActionRequest;
import com.java.slms.dto.StudentLeaveRequestDTO;
import com.java.slms.dto.StudentLeaveResponse;
import com.java.slms.model.Student;
import com.java.slms.util.LeaveStatus;

import java.util.List;

public interface StudentLeaveService
{
    void createLeaveRequest(StudentLeaveRequestDTO dto, Student student, Long schoolId);

    List<StudentLeaveResponse> getLeavesForLoggedInStudent(String panNumber, Long schoolId);

    void takeActionOnLeave(Long leaveId, Long teacherId, Long schoolId, LeaveActionRequest request);

    List<StudentLeaveResponse> getLeavesForTeacher(Long teacherId, LeaveStatus status, Long schoolId);

}
