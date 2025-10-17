package com.java.slms.controller;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.GalleryService;
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
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gallery Controller", description = "APIs for managing gallery items")
public class GalleryController
{

    private final GalleryService galleryService;

    @Operation(
            summary = "Add new gallery item",
            description = "Creates a new gallery image for a specific session and school.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Gallery item created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<GalleryResponseDto>> addGallery(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestBody GalleryRequestDto dto)
    {

        log.info("Creating new gallery item for session ID: {} and schoolId: {}", dto.getSessionId(), schoolId);
        GalleryResponseDto created = galleryService.addGallery(dto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<GalleryResponseDto>builder()
                        .data(created)
                        .message("Gallery item created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all gallery items (optionally filtered by session ID)",
            description = "Retrieves all gallery items for a school. If a sessionId is provided, filters gallery items by that session.",
            parameters = {
                    @Parameter(name = "sessionId", description = "ID of the session to filter gallery items", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gallery items retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<GalleryResponseDto>>> getAllGalleryItems(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestParam(required = false) Long sessionId)
    {

        log.info(sessionId != null
                ? "Fetching gallery items for session ID: {} and schoolId: {}"
                : "Fetching all gallery items for schoolId: {}", sessionId, schoolId);

        List<GalleryResponseDto> list = (sessionId != null)
                ? galleryService.getGalleryItemsBySessionId(sessionId, schoolId)
                : galleryService.getAllGalleryItems(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<GalleryResponseDto>>builder()
                        .data(list)
                        .message(sessionId != null
                                ? "Gallery items retrieved for session ID " + sessionId
                                : "All gallery items retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get gallery item by ID",
            description = "Fetches a single gallery item using its ID and schoolId.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the gallery item", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gallery item found"),
                    @ApiResponse(responseCode = "404", description = "Gallery item not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<GalleryResponseDto>> getGalleryById(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {

        log.info("Fetching gallery item with ID: {} and schoolId: {}", id, schoolId);
        GalleryResponseDto dto = galleryService.getGalleryById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<GalleryResponseDto>builder()
                        .data(dto)
                        .message("Gallery item found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete gallery item",
            description = "Deletes a gallery image by ID and schoolId.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gallery item deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Gallery item not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<String>> deleteGallery(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {

        log.info("Deleting gallery item with ID: {} and schoolId: {}", id, schoolId);
        galleryService.deleteGallery(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data(null)
                        .message("Gallery item deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Add multiple gallery images for a session",
            description = "Adds multiple image entries under a given session ID and school.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Gallery items created successfully"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<GalleryResponseDto>>> addBulkGalleryImages(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestBody BulkGalleryRequestDto dto)
    {

        log.info("Adding bulk gallery images for session ID: {} and schoolId: {}", dto.getSessionId(), schoolId);
        List<GalleryResponseDto> created = galleryService.addBulkGalleryImages(dto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<List<GalleryResponseDto>>builder()
                        .data(created)
                        .message("Bulk gallery images added")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }
}
