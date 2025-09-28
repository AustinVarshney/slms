package com.java.slms.dto;

import com.java.slms.model.Session;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CalendarRequestDto
{
    private LocalDate startDate;
    private LocalDate endDate;
    private String occasion;
    private Long sessionId;
}
