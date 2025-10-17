package com.java.slms.serviceImpl;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.CalendarEntity;
import com.java.slms.model.Session;
import com.java.slms.repository.CalendarRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService
{
    private final CalendarRepository calendarRepository;
    private final ModelMapper modelMapper;
    private final SessionRepository sessionRepository;

    @Override
    public CalendarResponseDto addCalendarEvent(CalendarRequestDto calendarRequestDto)
    {
        Session session = sessionRepository.findById(calendarRequestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + calendarRequestDto.getSessionId()));

        if (!session.isActive())
        {
            log.warn("Cannot add event to inactive session with ID {}", calendarRequestDto.getSessionId());
            throw new WrongArgumentException("Cannot add event to an inactive session");
        }

        CalendarEntity calendarEntity = modelMapper.map(calendarRequestDto, CalendarEntity.class);
        calendarEntity.setSession(session);
        calendarEntity.setId(null);
        calendarEntity = calendarRepository.save(calendarEntity);
        CalendarResponseDto calendarResponseDto = modelMapper.map(calendarEntity, CalendarResponseDto.class);
        calendarResponseDto.setSessionId(session.getId());
        return calendarResponseDto;
    }

    @Override
    public List<CalendarResponseDto> getAllCalendarEvents()
    {
        List<CalendarEntity> calendarEntityList = calendarRepository.findAll();
        return calendarEntityList.stream().map(calendarEntity -> {
            CalendarResponseDto dto = modelMapper.map(calendarEntity, CalendarResponseDto.class);
            if (calendarEntity.getSession() != null) {
                dto.setSessionId(calendarEntity.getSession().getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CalendarResponseDto getCalendarEventById(Long id)
    {
        CalendarEntity calendarEntity = calendarRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Calendar event not found"));
        CalendarResponseDto dto = modelMapper.map(calendarEntity, CalendarResponseDto.class);
        if (calendarEntity.getSession() != null) {
            dto.setSessionId(calendarEntity.getSession().getId());
        }
        return dto;
    }

    @Override
    public CalendarResponseDto updateCalendarEvent(Long id, CalendarRequestDto calendarRequestDto)
    {
        CalendarEntity calendarEntity = calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found with id: " + id));

        Session session = sessionRepository.findById(calendarRequestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + calendarRequestDto.getSessionId()));

        if (!session.isActive())
        {
            log.warn("Cannot update event for inactive session with ID {}", calendarRequestDto.getSessionId());
            throw new WrongArgumentException("Cannot update event for an inactive session");
        }

        // Manually update fields to avoid ID conflict
        calendarEntity.setStartDate(calendarRequestDto.getStartDate());
        calendarEntity.setEndDate(calendarRequestDto.getEndDate());
        calendarEntity.setOccasion(calendarRequestDto.getOccasion());
        calendarEntity.setSession(session);

        calendarEntity = calendarRepository.save(calendarEntity);

        CalendarResponseDto calendarResponseDto = modelMapper.map(calendarEntity, CalendarResponseDto.class);

        calendarResponseDto.setSessionId(session.getId());

        return calendarResponseDto;
    }

    @Override
    public void deleteCalendarEvent(Long id)
    {
        CalendarEntity calendarEntity = calendarRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Calendar event not found"));
        Session session = sessionRepository.findById(calendarEntity.getSession().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + calendarEntity.getSession().getId()));
        calendarRepository.delete(calendarEntity);
    }

    @Override
    public List<CalendarResponseDto> getCalendarEventsForCurrentSession()
    {
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        return calendarRepository.findBySession_Id(activeSession.getId())
                .stream()
                .map(calendar -> {
                    CalendarResponseDto dto = modelMapper.map(calendar, CalendarResponseDto.class);
                    dto.setSessionId(activeSession.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CalendarResponseDto> getCalendarEventsBySessionId(Long sessionId)
    {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return calendarRepository.findBySession_Id(sessionId)
                .stream()
                .map(calendar -> {
                    CalendarResponseDto dto = modelMapper.map(calendar, CalendarResponseDto.class);
                    dto.setSessionId(sessionId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
