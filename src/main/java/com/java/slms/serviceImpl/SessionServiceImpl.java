package com.java.slms.serviceImpl;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService
{

    private final SessionRepository sessionRepository;
    private final SchoolRepository schoolRepository;  // Make sure you have this repo
    private final ModelMapper modelMapper;

    @Override
    public SessionDto createSession(Long schoolId, CreateOrUpdateSessionRequest dto)
    {
        log.info("Creating session for school {} from {} to {}", schoolId, dto.getStartDate(), dto.getEndDate());

        if (sessionRepository.findBySchoolIdAndActiveTrue(schoolId).isPresent())
        {
            throw new WrongArgumentException("An active session already exists for this school. Deactivate it first.");
        }

        validateSessionLength(dto.getStartDate(), dto.getEndDate());

        if (sessionRepository.existsBySchoolIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(schoolId, dto.getEndDate(), dto.getStartDate()))
        {
            throw new AlreadyExistException("Session period overlaps with an existing session for this school.");
        }

        Session session = modelMapper.map(dto, Session.class);
        session.setActive(true);

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        session.setSchool(school);

        Session saved = sessionRepository.save(session);
        log.info("Session created with ID {} for school {}", saved.getId(), schoolId);

        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public SessionDto updateSession(Long id, Long schoolId, CreateOrUpdateSessionRequest dto)
    {
        log.info("Updating session {} for school {}", id, schoolId);

        Session existing = sessionRepository.findActiveSessionByIdAndSchoolId(id, schoolId).orElseThrow(() -> new WrongArgumentException("Session not found or does not belong to the provided school, or is not active."));

        validateSessionLength(dto.getStartDate(), dto.getEndDate());

//        boolean isOverlapping = sessionRepository.existsBySchoolIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(schoolId, dto.getEndDate(), dto.getStartDate());
//
//        boolean sameDates = dto.getStartDate().isEqual(existing.getStartDate()) && dto.getEndDate().isEqual(existing.getEndDate());
//
//        if (isOverlapping && !sameDates)
//        {
//            throw new WrongArgumentException("Updated session period overlaps with another existing session.");
//        }

        modelMapper.map(dto, existing);
        Session saved = sessionRepository.save(existing);

        log.info("Session {} updated for school {}", saved.getId(), schoolId);
        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public void deleteSession(Long schoolId, Long id)
    {
        log.info("Deleting session {} for school {}", id, schoolId);

        Session session = sessionRepository.findBySessionIdAndSchoolId(id, schoolId).orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id + " for schoolId: " + schoolId));
        if (session.isActive())
        {
            throw new WrongArgumentException("Cannot delete an active session. Deactivate it first.");
        }
        sessionRepository.deleteById(session.getId());
        log.info("Session {} deleted for school {}", id, schoolId);
    }

    @Override
    public List<SessionDto> getAllSessions(Long schoolId)
    {
        return sessionRepository.findAllBySchoolId(schoolId).stream().map(session -> modelMapper.map(session, SessionDto.class)).toList();
    }

    @Override
    public SessionDto getSessionById(Long schoolId, Long id)
    {
        Session session = sessionRepository.findActiveSessionByIdAndSchoolId(id, schoolId).orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id + " for schoolId: " + schoolId));

        return modelMapper.map(session, SessionDto.class);
    }

    @Override
    public SessionDto getCurrentSession(Long schoolId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found for school with ID: " + schoolId));
        return modelMapper.map(session, SessionDto.class);
    }

    @Transactional
    @Override
    public void deactivateCurrentSession(Long schoolId)
    {
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found to deactivate for school with ID: " + schoolId));

        activeSession.setActive(false);
        sessionRepository.save(activeSession);



        log.info("Deactivated session {} for school {}", activeSession.getId(), schoolId);
    }

    private void validateSessionLength(LocalDate start, LocalDate end)
    {
        long monthsBetween = ChronoUnit.MONTHS.between(start, end);
        if (monthsBetween != 12)
        {
            throw new WrongArgumentException("Session length must be exactly 12 months.");
        }
    }
}
