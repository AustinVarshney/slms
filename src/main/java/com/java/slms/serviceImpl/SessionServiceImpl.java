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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService
{

    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;

    @Override
    public SessionDto createSession(CreateOrUpdateSessionRequest dto)
    {
        log.info("Attempting to create new session from {} to {}", dto.getStartDate(), dto.getEndDate());

        if (sessionRepository.findByActiveTrue().isPresent())
        {
            throw new WrongArgumentException("An active session already exists. Please deactivate it before creating a new one.");
        }

        validateSessionLength(dto.getStartDate(), dto.getEndDate());

        if (isOverlapping(dto.getStartDate(), dto.getEndDate()))
        {
            throw new AlreadyExistException("Session period overlaps with an existing session.");
        }

        Session session = modelMapper.map(dto, Session.class);
        session.setActive(true);

        Session saved = sessionRepository.save(session);
        log.info("Session created successfully with ID: {}", saved.getId());

        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public SessionDto updateSession(Long id, CreateOrUpdateSessionRequest request)
    {
        log.info("Updating session with ID: {}", id);

        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));

        if (!existing.isActive())
        {
            throw new WrongArgumentException("Only the active session can be updated.");
        }

        validateSessionLength(request.getStartDate(), request.getEndDate());

        boolean isOverlapping = isOverlapping(request.getStartDate(), request.getEndDate());
        boolean sameDates = request.getStartDate().isEqual(existing.getStartDate())
                && request.getEndDate().isEqual(existing.getEndDate());

        if (isOverlapping && !sameDates)
        {
            throw new WrongArgumentException("Updated session period overlaps with another existing session.");
        }

        modelMapper.map(request, existing);
        Session saved = sessionRepository.save(existing);

        log.info("Session with ID {} updated successfully", saved.getId());
        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public void deleteSession(Long id)
    {
        log.info("Attempting to delete session with ID: {}", id);

        if (!sessionRepository.existsById(id))
        {
            throw new ResourceNotFoundException("Session not found with ID: " + id);
        }

        sessionRepository.deleteById(id);
        log.info("Session with ID {} deleted", id);
    }

    @Override
    public List<SessionDto> getAllSessions()
    {
        return sessionRepository.findAll().stream()
                .map(session -> modelMapper.map(session, SessionDto.class))
                .toList();
    }

    @Override
    public SessionDto getSessionById(Long id)
    {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));
        return modelMapper.map(session, SessionDto.class);
    }

    @Override
    public SessionDto getCurrentSession()
    {
        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
        return modelMapper.map(session, SessionDto.class);
    }

    @Transactional
    @Override
    public void deactivateCurrentSession()
    {
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found to deactivate"));

        activeSession.setActive(false);
        sessionRepository.save(activeSession);

        log.info("Deactivated session with ID: {}", activeSession.getId());
    }

    private void validateSessionLength(LocalDate start, LocalDate end)
    {
        if (!end.equals(start.plusYears(1).minusDays(1)))
        {
            throw new WrongArgumentException("Session length must be exactly 12 months.");
        }
    }

    private boolean isOverlapping(LocalDate start, LocalDate end)
    {
        return sessionRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
    }
}
