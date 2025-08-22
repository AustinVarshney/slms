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
    private DayOfWeek day; // e.g. MONDAY, TUESDAY
    private LocalTime startTime;
    private LocalTime endTime;
}