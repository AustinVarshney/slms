package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentLeaveRequestDTO
{
    private String studentPan;
    private Long sessionId;
    private Long teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
