package com.java.slms.serviceImpl;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.CalendarEntity;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.repository.CalendarRepository;
import com.java.slms.repository.SchoolRepository;
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
    private final SchoolRepository schoolRepository;

    @Override
    public CalendarResponseDto addCalendarEvent(CalendarRequestDto calendarRequestDto, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with Id: " + schoolId));

        // Find active session for the given school
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));

        CalendarEntity calendarEntity = modelMapper.map(calendarRequestDto, CalendarEntity.class);
        calendarEntity.setSession(session);
        calendarEntity.setId(null);
        calendarEntity.setSchool(school);
        calendarEntity = calendarRepository.save(calendarEntity);

        CalendarResponseDto calendarResponseDto = modelMapper.map(calendarEntity, CalendarResponseDto.class);
        calendarResponseDto.setSessionId(session.getId());
        calendarResponseDto.setSchoolId(schoolId);
        return calendarResponseDto;
    }

    @Override
    public List<CalendarResponseDto> getAllCalendarEvents(Long schoolId)
    {
        // Fetch all events filtered by schoolId
        List<CalendarEntity> calendarEntityList = calendarRepository.findAllBySchoolId(schoolId);

        return calendarEntityList.stream()
                .map(calendarEntity -> modelMapper.map(calendarEntity, CalendarResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CalendarResponseDto getCalendarEventById(Long id, Long schoolId)
    {
        // Find event by id AND schoolId to prevent cross-school access
        CalendarEntity calendarEntity = calendarRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found with id: " + id + " for school: " + schoolId));

        return modelMapper.map(calendarEntity, CalendarResponseDto.class);
    }

    @Override
    public CalendarResponseDto updateCalendarEvent(Long id, CalendarRequestDto calendarRequestDto, Long schoolId)
    {
        // Find existing event by id and schoolId
        CalendarEntity calendarEntity = calendarRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found with id: " + id + " for school: " + schoolId));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));

        modelMapper.map(calendarRequestDto, calendarEntity);
        calendarEntity.setSession(session);

        calendarEntity = calendarRepository.save(calendarEntity);

        CalendarResponseDto calendarResponseDto = modelMapper.map(calendarEntity, CalendarResponseDto.class);
        calendarResponseDto.setSessionId(session.getId());
        calendarResponseDto.setSchoolId(schoolId);

        return calendarResponseDto;
    }

    @Override
    public void deleteCalendarEvent(Long id, Long schoolId)
    {
        CalendarEntity calendarEntity = calendarRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found with id: " + id + " for school: " + schoolId));

        calendarRepository.delete(calendarEntity);
    }

    @Override
    public List<CalendarResponseDto> getCalendarEventsForCurrentSession(Long schoolId)
    {
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school id: " + schoolId));

        List<CalendarEntity> calendarEntities = calendarRepository.findBySessionIdAndSchoolId(activeSession.getId(), schoolId);

        return calendarEntities.stream()
                .map(calendar -> modelMapper.map(calendar, CalendarResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CalendarResponseDto> getCalendarEventsBySessionId(Long sessionId, Long schoolId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found in schoolId: " + schoolId));

        List<CalendarEntity> calendarEntities = calendarRepository.findBySessionIdAndSchoolId(sessionId, schoolId);

        return calendarEntities.stream()
                .map(calendar -> modelMapper.map(calendar, CalendarResponseDto.class))
                .collect(Collectors.toList());
    }
}
