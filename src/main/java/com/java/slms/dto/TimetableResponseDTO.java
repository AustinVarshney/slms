package com.java.slms.dto;

import com.java.slms.util.DayOfWeek;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimetableResponseDTO
{
    private Long id;
    private DayOfWeek day;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private LocalTime startTime;
    private LocalTime endTime;
}