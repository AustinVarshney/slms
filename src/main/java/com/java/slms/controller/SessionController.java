package com.java.slms.controller;

import com.java.slms.dto.SessionDto;
import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class SessionController
{

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SessionDto>> createSession(
            @RequestBody CreateOrUpdateSessionRequest request)
    {
        SessionDto saved = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SessionDto>builder()
                        .data(saved)
                        .message("Session created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionDto>>> getAllSessions()
    {
        List<SessionDto> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(
                ApiResponse.<List<SessionDto>>builder()
                        .data(sessions)
                        .message("All sessions fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionDto>> getSessionById(@PathVariable Long id)
    {
        SessionDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(
                ApiResponse.<SessionDto>builder()
                        .data(session)
                        .message("Session fetched by ID")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionDto>> updateSession(
            @PathVariable Long id,
            @RequestBody CreateOrUpdateSessionRequest request)
    {
        SessionDto updated = sessionService.updateSession(id, request);
        return ResponseEntity.ok(
                ApiResponse.<SessionDto>builder()
                        .data(updated)
                        .message("Session updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long id)
    {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Session deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
