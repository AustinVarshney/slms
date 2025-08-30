package com.java.slms.serviceImpl;

import com.java.slms.dto.CreateOrUpdateEventRequest;
import com.java.slms.dto.EventDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Event;
import com.java.slms.model.Session;
import com.java.slms.repository.EventRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService
{

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final SessionRepository sessionRepository;

    @Override
    public EventDto createEvent(CreateOrUpdateEventRequest request)
    {
        Event event = modelMapper.map(request, Event.class);

        // Get the active session
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        event.setSession(activeSession);
        Event saved = eventRepository.save(event);
        EventDto dto = modelMapper.map(saved, EventDto.class);

        dto.setSessionId(activeSession.getId());
        dto.setSessionName(activeSession.getName());

        return dto;
    }

    @Override
    public EventDto updateEvent(Long id, CreateOrUpdateEventRequest request)
    {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());

        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        event.setSession(activeSession);

        Event saved = eventRepository.save(event);
        EventDto dto = modelMapper.map(saved, EventDto.class);

        // Set session info in DTO
        dto.setSessionId(activeSession.getId());
        dto.setSessionName(activeSession.getName());

        return dto;
    }

    @Override
    public void deleteEvent(Long id)
    {
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        if (event.getSession() == null || !event.getSession().getId().equals(activeSession.getId()))
        {
            throw new ResourceNotFoundException("Event with ID " + id + " does not belong to the current active session and cannot be deleted.");
        }

        eventRepository.delete(event);
    }

    @Override
    public EventDto getEventById(Long id)
    {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        EventDto dto = modelMapper.map(event, EventDto.class);
        if (event.getSession() != null)
        {
            dto.setSessionId(event.getSession().getId());
            dto.setSessionName(event.getSession().getName());
        }
        return dto;
    }

    @Override
    public List<EventDto> getAllEvents()
    {
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        // Fetch events only for the active session
        return eventRepository.findBySession_Id(activeSession.getId()).stream()
                .map(event ->
                {
                    EventDto dto = modelMapper.map(event, EventDto.class);
                    dto.setSessionId(activeSession.getId());
                    dto.setSessionName(activeSession.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDto> getEventsBySessionId(Long sessionId)
    {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return eventRepository.findBySession_Id(sessionId).stream()
                .map(event ->
                {
                    EventDto dto = modelMapper.map(event, EventDto.class);
                    dto.setSessionId(session.getId());
                    dto.setSessionName(session.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }


}
