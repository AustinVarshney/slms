package com.java.slms.dto;

import com.java.slms.util.DayOfWeek;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimetableRequestDTO
{
    private Long classId;
    private Long subjectId;
    private DayOfWeek day; // e.g. MONDAY, TUESDAY
    private LocalTime startTime;
    private LocalTime endTime;
}