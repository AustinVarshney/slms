package com.java.slms.dto;

import com.java.slms.util.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentLeaveResponse
{
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private int daysRequested;

    private String reason;
    private String classTeacherResponse;

    private LeaveStatus status;

    private LocalDateTime processedAt;

    private String classTeacherName;
    private String sessionName;
}
