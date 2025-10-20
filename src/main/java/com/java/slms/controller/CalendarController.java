package com.java.slms.controller;

import com.java.slms.dto.CalendarRequestDto;
import com.java.slms.dto.CalendarResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar Controller", description = "APIs for managing calendar events")
@Slf4j
public class CalendarController
{
    private final CalendarService calendarService;

    @Operation(
            summary = "Add a new calendar event",
            description = "Creates a new calendar entry for a specific session (e.g., class, exam, holiday).",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Calendar event created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<CalendarResponseDto>> addCalendarEvent(
            @RequestBody CalendarRequestDto calendarRequestDto
            , @RequestAttribute("schoolId") Long schoolId)
    {
        CalendarResponseDto created = calendarService.addCalendarEvent(calendarRequestDto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<CalendarResponseDto>builder()
                        .data(created)
                        .message("Calendar event created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all calendar events",
            description = "Fetches all calendar events.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar events retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<CalendarResponseDto>>> getAllCalendarEvents(
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<CalendarResponseDto> list = calendarService.getAllCalendarEvents(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<CalendarResponseDto>>builder()
                        .data(list)
                        .message("Total calendar events: " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get calendar event by ID",
            description = "Fetches a particular calendar event by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the calendar event", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar event found"),
                    @ApiResponse(responseCode = "400", description = "Invalid event ID or event not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<CalendarResponseDto>> getCalendarEventById(
            @PathVariable Long id
            , @RequestAttribute("schoolId") Long schoolId)
    {
        CalendarResponseDto dto = calendarService.getCalendarEventById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<CalendarResponseDto>builder()
                        .data(dto)
                        .message("Calendar event found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update calendar event",
            description = "Updates a calendar event by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the calendar event", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar event updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or event not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<CalendarResponseDto>> updateCalendarEvent(
            @PathVariable Long id,
            @RequestBody CalendarRequestDto calendarRequestDto
            , @RequestAttribute("schoolId") Long schoolId)
    {
        CalendarResponseDto updated = calendarService.updateCalendarEvent(id, calendarRequestDto, schoolId);
        return ResponseEntity.ok(
                RestResponse.<CalendarResponseDto>builder()
                        .data(updated)
                        .message("Calendar event updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete calendar event",
            description = "Deletes a calendar event by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the calendar event", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar event deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid event ID or event not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<String>> deleteCalendarEvent(
            @PathVariable Long id
            , @RequestAttribute("schoolId") Long schoolId)
    {
        calendarService.deleteCalendarEvent(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data(null)
                        .message("Calendar event deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get calendar events (optionally filtered by session ID)",
            description = "Fetches all calendar events. If sessionId is provided, filters calendar events by that session.",
            parameters = {
                    @Parameter(name = "sessionId", description = "Session ID to filter calendar events", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar events retrieved"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @GetMapping("/by-session")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<CalendarResponseDto>>> getCalendarEventsBySessionId(
            @RequestParam(required = false) Long sessionId
            , @RequestAttribute("schoolId") Long schoolId)
    {
        log.info(sessionId != null
                ? "Fetching calendar events for session ID: {}"
                : "Fetching all calendar events (no session filter)", sessionId);

        List<CalendarResponseDto> events = (sessionId != null)
                ? calendarService.getCalendarEventsBySessionId(sessionId, schoolId)
                : calendarService.getAllCalendarEvents(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<CalendarResponseDto>>builder()
                        .data(events)
                        .message(sessionId != null
                                ? "Calendar events retrieved for session ID " + sessionId
                                : "All calendar events retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get calendar events for the current active session",
            description = "Fetches calendar events for the current active session. Assumes only one session is active at a time.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calendar events retrieved for current session"),
                    @ApiResponse(responseCode = "404", description = "No active session found", content = @Content)
            }
    )
    @GetMapping("/current-session")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<CalendarResponseDto>>> getCalendarEventsForCurrentSession(
            @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Fetching calendar events for the current active session");

        List<CalendarResponseDto> events = calendarService.getCalendarEventsForCurrentSession(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<CalendarResponseDto>>builder()
                        .data(events)
                        .message("Calendar events retrieved for the current session")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
