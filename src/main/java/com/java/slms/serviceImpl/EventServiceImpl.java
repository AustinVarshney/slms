package com.java.slms.serviceImpl;

import com.java.slms.dto.CreateOrUpdateEventRequest;
import com.java.slms.dto.EventDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Event;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.repository.EventRepository;
import com.java.slms.repository.SchoolRepository;
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
    private final SchoolRepository schoolRepository;

    @Override
    public EventDto createEvent(CreateOrUpdateEventRequest request, Long schoolId)
    {
        Event event = modelMapper.map(request, Event.class);

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));

        event.setSession(activeSession);
        event.setSchool(school);
        Event savedEvent = eventRepository.save(event);

        return mapToDtoWithSession(savedEvent, activeSession);
    }

    @Override
    public EventDto updateEvent(Long id, CreateOrUpdateEventRequest request, Long schoolId)
    {
        Event event = eventRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());

        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));

        event.setSession(activeSession);

        Event updatedEvent = eventRepository.save(event);
        return mapToDtoWithSession(updatedEvent, activeSession);
    }

    @Override
    public void deleteEvent(Long id, Long schoolId)
    {
        Event event = eventRepository.findByIdAndSchoolIdAndSessionActive(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event with ID '" + id + "' not found for the given school (ID: " + schoolId + ") or the associated session is not active."
                ));

        eventRepository.delete(event);
    }

    @Override
    public EventDto getEventById(Long id, Long schoolId)
    {
        Event event = eventRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));
        return mapToDtoWithSession(event, session);
    }

    @Override
    public List<EventDto> getAllEvents(Long schoolId)
    {
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school ID: " + schoolId));

        return eventRepository.findBySchoolIdAndSessionActive(schoolId)
                .stream()
                .map(event -> mapToDtoWithSession(event, activeSession))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDto> getEventsBySessionId(Long sessionId, Long schoolId)
    {
        Session session = sessionRepository.findBySessionIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return eventRepository.findBySessionIdAndSchoolId(sessionId, schoolId)
                .stream()
                .map(event -> mapToDtoWithSession(event, session))
                .collect(Collectors.toList());
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
