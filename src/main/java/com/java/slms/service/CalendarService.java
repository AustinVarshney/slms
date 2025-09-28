package com.java.slms.service;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;

import java.util.List;

public interface CalendarService
{
    CalendarResponseDto addCalendarEvent(CalendarRequestDto calendarRequestDto);

    List<CalendarResponseDto> getAllCalendarEvents();

    CalendarResponseDto getCalendarEventById(Long id);

    CalendarResponseDto updateCalendarEvent(Long id, CalendarRequestDto calendarRequestDto);

    void deleteCalendarEvent(Long id);
}
