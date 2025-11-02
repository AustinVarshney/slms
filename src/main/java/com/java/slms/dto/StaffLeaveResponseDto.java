package com.java.slms.dto;

import com.java.slms.util.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StaffLeaveResponseDto
{
    private Long id;
    private Long teacherId;
    private String teacherName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysRequested;
    private LeaveStatus status;
    private String reason;
    private String adminResponse;
    private Long sessionId;
    private String sessionName;

    private int totalLeavesAllowed;
    private int remainingLeavesBalance;
}
