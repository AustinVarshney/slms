package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CalendarResponseDto
{
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String occasion;
    private Long sessionId;
    private Long schoolId;
}
