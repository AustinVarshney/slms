package com.java.slms.dto;

import com.java.slms.util.FeeMonth;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttendanceByClassDto
{
    private Long id;
    private Long classId;
    private String className;
    private Long sessionId;
    private String sessionName;
    private FeeMonth month;
    List<StudentAttendance> studentAttendances;
}
