package com.java.slms.controller;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Session Controller", description = "APIs for session management")
@Slf4j
public class SessionController
{

    private final SessionService sessionService;

    @Operation(
            summary = "Create a new session",
            description = "Creates a new academic/session period.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Session created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or active session exists", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<RestResponse<SessionDto>> createSession(
            @RequestBody CreateOrUpdateSessionRequest request)
    {
        log.info("Attempting to create a new session from {} to {}", request.getStartDate(), request.getEndDate());
        SessionDto saved = sessionService.createSession(request);
        log.info("Session created with ID: {}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<SessionDto>builder()
                        .data(saved)
                        .message("Session created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all sessions",
            description = "Fetches all academic/session periods.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All sessions fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<SessionDto>>> getAllSessions()
    {
        log.info("Fetching all sessions");
        List<SessionDto> sessions = sessionService.getAllSessions();
        log.info("Retrieved {} sessions", sessions.size());

        return ResponseEntity.ok(
                RestResponse.<List<SessionDto>>builder()
                        .data(sessions)
                        .message("All sessions fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get session by ID",
            description = "Retrieves a session by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the session", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Session not found or invalid ID", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<SessionDto>> getSessionById(@PathVariable Long id)
    {
        log.info("Fetching session with ID: {}", id);
        SessionDto session = sessionService.getSessionById(id);
        log.info("Session fetched successfully: ID {}", id);

        return ResponseEntity.ok(
                RestResponse.<SessionDto>builder()
                        .data(session)
                        .message("Session fetched by ID")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update session",
            description = "Updates an existing session's details.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the session to update", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or session not active", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<SessionDto>> updateSession(
            @PathVariable Long id,
            @RequestBody CreateOrUpdateSessionRequest request)
    {
        log.info("Updating session with ID: {}", id);
        SessionDto updated = sessionService.updateSession(id, request);
        log.info("Session updated: ID {}", id);

        return ResponseEntity.ok(
                RestResponse.<SessionDto>builder()
                        .data(updated)
                        .message("Session updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Deactivate current active session",
            description = "Deactivates the currently active session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session deactivated successfully"),
                    @ApiResponse(responseCode = "400", description = "No active session found to deactivate", content = @Content)
            }
    )
    @PutMapping("/deactivate")
    public ResponseEntity<RestResponse<Void>> deactivateCurrentSession()
    {
        log.info("Deactivating current active session");
        sessionService.deactivateCurrentSession();
        log.info("Current session deactivated");

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Session Deactivated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
