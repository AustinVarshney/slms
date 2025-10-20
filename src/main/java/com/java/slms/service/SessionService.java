package com.java.slms.service;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SessionService
{
    SessionDto createSession(Long schoolId, CreateOrUpdateSessionRequest dto);

    SessionDto updateSession(Long id, Long schoolId, CreateOrUpdateSessionRequest dto);

    void deleteSession(Long schoolId, Long id);

    List<SessionDto> getAllSessions(Long schoolId);

    SessionDto getSessionById(Long schoolId, Long id);

    SessionDto getCurrentSession(Long schoolId);

    @Transactional
    void deactivateCurrentSession(Long schoolId);
}
