package com.java.slms.dto;

import com.java.slms.util.FeeMonth;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class CurrentDayAttendance
{
    private Long id;
    private Long classId;
    private String className;
    private LocalDate date;
    List<StudentAttendance> studentAttendances;
}
