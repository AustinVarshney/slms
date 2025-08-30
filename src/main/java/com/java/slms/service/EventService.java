package com.java.slms.service;

import com.java.slms.dto.CreateOrUpdateEventRequest;
import com.java.slms.dto.EventDto;

import java.util.List;

public interface EventService
{
    EventDto createEvent(CreateOrUpdateEventRequest request);

    EventDto updateEvent(Long id, CreateOrUpdateEventRequest request);

    void deleteEvent(Long id);

    EventDto getEventById(Long id);

    List<EventDto> getAllEvents();

    List<EventDto> getEventsBySessionId(Long sessionId);

}