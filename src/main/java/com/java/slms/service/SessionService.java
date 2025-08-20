package com.java.slms.service;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import com.java.slms.model.Session;

import java.util.List;

public interface SessionService
{
    SessionDto createSession(CreateOrUpdateSessionRequest dto);

    SessionDto updateSession(Long id, CreateOrUpdateSessionRequest dto);

    void deleteSession(Long id);

    List<SessionDto> getAllSessions();

    SessionDto getSessionById(Long id);
}

