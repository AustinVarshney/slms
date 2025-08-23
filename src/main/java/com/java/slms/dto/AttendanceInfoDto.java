package com.java.slms.dto;

import com.java.slms.util.FeeMonth;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttendanceInfoDto
{
    private String panNumber;
    private String studentName;
    private Long sessionId;
    private String sessionName;
    private Long classId;
    private String className;
    private FeeMonth month;
    List<AttendenceResponse> attendances;
}
