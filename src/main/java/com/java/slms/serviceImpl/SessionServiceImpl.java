package com.java.slms.serviceImpl;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Session;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService
{

    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;

    @Override
    public SessionDto createSession(CreateOrUpdateSessionRequest dto)
    {
        Optional<Session> activeSessionOpt = sessionRepository.findByActiveTrue();

        if (activeSessionOpt.isPresent())
        {
            throw new WrongArgumentException("An active session already exists. Please close all active sessions before creating a new one.");
        }
        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();

        long months = ChronoUnit.MONTHS.between(
                YearMonth.from(start),
                YearMonth.from(end)
        );

        if (months == 12)
        {
            throw new WrongArgumentException("Session length must be 12 months.");
        }

        boolean overlap = sessionRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                end, start
        );
        if (overlap)
        {
            throw new AlreadyExistException("Session period overlaps with another existing session.");
        }

        Session session = modelMapper.map(dto, Session.class);
        Session saved = sessionRepository.save(session);
        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public SessionDto updateSession(Long id, CreateOrUpdateSessionRequest request)
    {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        long months = ChronoUnit.MONTHS.between(
                YearMonth.from(start),
                YearMonth.from(end)
        );

        if (months == 12)
        {
            throw new WrongArgumentException("Session length must be 12 months.");
        }

        boolean overlap = sessionRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                end, start
        );
        if (overlap && !(start.isEqual(existing.getStartDate()) && end.isEqual(existing.getEndDate())))
        {
            throw new WrongArgumentException("Session period overlaps with another existing session.");
        }

        modelMapper.map(request, existing);
        Session saved = sessionRepository.save(existing);
        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public void deleteSession(Long id)
    {
        if (!sessionRepository.existsById(id))
        {
            throw new ResourceNotFoundException("Session not found");
        }
        sessionRepository.deleteById(id);
    }

    @Override
    public List<SessionDto> getAllSessions()
    {
        return sessionRepository.findAll()
                .stream()
                .map(session -> modelMapper.map(session, SessionDto.class))
                .toList();
    }

    @Override
    public SessionDto getSessionById(Long id)
    {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return modelMapper.map(session, SessionDto.class);
    }

    @Override
    public SessionDto getCurrentSession()
    {
        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
        return modelMapper.map(session, SessionDto.class);
    }
}