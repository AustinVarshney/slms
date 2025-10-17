package com.java.slms.service;

import com.java.slms.dto.CreateOrUpdateEventRequest;
import com.java.slms.dto.EventDto;

import java.util.List;

public interface EventService
{

    EventDto createEvent(CreateOrUpdateEventRequest request, Long schoolId);

    EventDto updateEvent(Long eventId, CreateOrUpdateEventRequest request, Long schoolId);

    void deleteEvent(Long eventId, Long schoolId);

    EventDto getEventById(Long eventId, Long schoolId);

    List<EventDto> getEventsBySessionId(Long sessionId, Long schoolId);

    List<EventDto> getAllEvents(Long schoolId);
}
