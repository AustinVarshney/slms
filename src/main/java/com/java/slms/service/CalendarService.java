package com.java.slms.service;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;

import java.util.List;

public interface CalendarService
{
    CalendarResponseDto addCalendarEvent(CalendarRequestDto calendarRequestDto, Long schoolId);

    List<CalendarResponseDto> getAllCalendarEvents(Long schoolId);

    CalendarResponseDto getCalendarEventById(Long id, Long schoolId);

    CalendarResponseDto updateCalendarEvent(Long id, CalendarRequestDto calendarRequestDto, Long schoolId);

    void deleteCalendarEvent(Long id, Long schoolId);

    List<CalendarResponseDto> getCalendarEventsBySessionId(Long sessionId, Long schoolId);

    List<CalendarResponseDto> getCalendarEventsForCurrentSession(Long schoolId);
}
