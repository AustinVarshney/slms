package com.java.slms.controller;

import com.java.slms.dto.CreateOrUpdateEventRequest;
import com.java.slms.dto.EventDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events Controller", description = "API for managing school events")
public class EventController
{

    private final EventService eventService;

    @Operation(
            summary = "Create a new event",
            description = "Adds a new school event with date range and session assignment",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<EventDto>> createEvent(@RequestBody CreateOrUpdateEventRequest request,
                                                              @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Creating event: {} for schoolId: {}", request.getTitle(), schoolId);
        EventDto saved = eventService.createEvent(request, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<EventDto>builder()
                        .data(saved)
                        .message("Event created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update an existing event",
            description = "Modifies the details of an event including session assignment",
            parameters = {
                    @Parameter(name = "id", description = "Event ID", required = true),
                    @Parameter(name = "schoolId", description = "School ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<EventDto>> updateEvent(@PathVariable Long id,
                                                              @RequestBody CreateOrUpdateEventRequest request,
                                                              @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Updating event ID: {} for schoolId: {}", id, schoolId);
        EventDto updated = eventService.updateEvent(id, request, schoolId);
        return ResponseEntity.ok(
                RestResponse.<EventDto>builder()
                        .data(updated)
                        .message("Event updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Delete an event by ID in Current Session",
            description = "Deletes a specific event associated with the current session",
            parameters = {
                    @Parameter(name = "eventId", description = "Event ID", required = true),
                    @Parameter(name = "schoolId", description = "School ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Event or session not found", content = @Content)
            }
    )
    public ResponseEntity<RestResponse<String>> deleteEventByIdAndSessionId(@PathVariable Long eventId,
                                                                            @RequestAttribute("schoolId") Long schoolId)
    {
        eventService.deleteEvent(eventId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data(null)
                        .message("Event deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get event by ID",
            description = "Returns details of a specific event including session info",
            parameters = {
                    @Parameter(name = "id", description = "Event ID", required = true),
                    @Parameter(name = "schoolId", description = "School ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event found"),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<EventDto>> getEventById(@PathVariable Long id,
                                                               @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Fetching event with ID: {} for schoolId: {}", id, schoolId);
        EventDto event = eventService.getEventById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<EventDto>builder()
                        .data(event)
                        .message("Event fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get events (optionally filtered by session ID)",
            description = "Fetches all events. If sessionId is provided, filters events by that session.",
            parameters = {
                    @Parameter(name = "sessionId", description = "Optional session ID"),
                    @Parameter(name = "schoolId", description = "School ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events retrieved"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<EventDto>>> getEvents(@RequestParam(required = false) Long sessionId,
                                                                  @RequestAttribute("schoolId") Long schoolId)
    {
        log.info(sessionId != null
                ? "Fetching events for session ID: {} and schoolId: {}"
                : "Fetching all events for schoolId: {}", sessionId, schoolId);

        List<EventDto> events = (sessionId != null)
                ? eventService.getEventsBySessionId(sessionId, schoolId)
                : eventService.getAllEvents(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<EventDto>>builder()
                        .data(events)
                        .message(sessionId != null
                                ? "Events retrieved for session ID " + sessionId
                                : "All events retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
