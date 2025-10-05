package com.java.slms.dto;

import com.java.slms.util.DayOfWeek;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimetableRequestDTO
{
    private Long classId;
    private Long subjectId;
    private Long teacherId;
    private DayOfWeek day; // e.g. MONDAY, TUESDAY
    private Integer period; // Period number (1, 2, 3, etc.)
    private LocalTime startTime;
    private LocalTime endTime;
}