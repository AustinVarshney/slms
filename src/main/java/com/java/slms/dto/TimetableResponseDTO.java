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
    private String dayOfWeek; // String representation of day (e.g., "Monday", "Tuesday")
    private Long classId;
    private String className;
    private String section;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String teacherContactNumber; // Teacher's contact/mobile number
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalStudents; // Total students in the class
    private Integer period; // Period number
}