package com.java.slms.service;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;

import java.util.List;

public interface CalendarService
{
    CalendarResponseDto addCalendarEvent(CalendarRequestDto calendarRequestDto);

    List<CalendarResponseDto> getAllCalendarEvents();

    List<CalendarResponseDto> getCalendarEventsBySessionId(Long sessionId);

    CalendarResponseDto getCalendarEventById(Long id);

    CalendarResponseDto updateCalendarEvent(Long id, CalendarRequestDto calendarRequestDto);

    void deleteCalendarEvent(Long id);

    List<CalendarResponseDto> getCalendarEventsForCurrentSession();
}
