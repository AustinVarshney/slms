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
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;

    @Override
    public EventDto createEvent(CreateOrUpdateEventRequest request)
    {
        Event event = modelMapper.map(request, Event.class);
        Session activeSession = getActiveSession();

        event.setSession(activeSession);
        Event savedEvent = eventRepository.save(event);

        return mapToDtoWithSession(savedEvent, activeSession);
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

        Session activeSession = getActiveSession();
        event.setSession(activeSession);

        Event updatedEvent = eventRepository.save(event);
        return mapToDtoWithSession(updatedEvent, activeSession);
    }

    @Override
    public void deleteEvent(Long id)
    {
        Session activeSession = getActiveSession();

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

        Session session = event.getSession();
        return mapToDtoWithSession(event, session);
    }

    @Override
    public List<EventDto> getAllEvents()
    {
        Session activeSession = getActiveSession();

        return eventRepository.findBySession_Id(activeSession.getId())
                .stream()
                .map(event -> mapToDtoWithSession(event, activeSession))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDto> getEventsBySessionId(Long sessionId)
    {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return eventRepository.findBySession_Id(sessionId)
                .stream()
                .map(event -> mapToDtoWithSession(event, session))
                .collect(Collectors.toList());
    }

    private Session getActiveSession()
    {
        return sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
    }

    private EventDto mapToDtoWithSession(Event event, Session session)
    {
        EventDto dto = modelMapper.map(event, EventDto.class);
        if (session != null)
        {
            dto.setSessionId(session.getId());
            dto.setSessionName(session.getName());
        }
        return dto;
    }
}
