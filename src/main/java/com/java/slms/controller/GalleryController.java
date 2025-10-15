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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            description = "Creates a new gallery image for a specific session.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Gallery item created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<GalleryResponseDto>> addGallery(@RequestBody GalleryRequestDto dto)
    {
        log.info("Creating new gallery item for session ID: {}", dto.getSessionId());
        GalleryResponseDto created = galleryService.addGallery(dto);
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
            description = "Retrieves all gallery items. If a sessionId is provided, filters gallery items by that session.",
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
            @RequestParam(required = false) Long sessionId
    )
    {
        log.info(sessionId != null
                ? "Fetching gallery items for session ID: {}"
                : "Fetching all gallery items", sessionId);

        List<GalleryResponseDto> list = (sessionId != null)
                ? galleryService.getGalleryItemsBySessionId(sessionId)
                : galleryService.getAllGalleryItems();

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
            description = "Fetches a single gallery item using its ID.",
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
    public ResponseEntity<RestResponse<GalleryResponseDto>> getGalleryById(@PathVariable Long id)
    {
        log.info("Fetching gallery item with ID: {}", id);
        GalleryResponseDto dto = galleryService.getGalleryById(id);
        return ResponseEntity.ok(
                RestResponse.<GalleryResponseDto>builder()
                        .data(dto)
                        .message("Gallery item found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update gallery item",
            description = "Updates an existing gallery image by ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the gallery item", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gallery item updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or gallery item not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<GalleryResponseDto>> updateGallery(@PathVariable Long id, @RequestBody GalleryRequestDto dto)
    {
        log.info("Updating gallery item with ID: {}", id);
        GalleryResponseDto updated = galleryService.updateGallery(id, dto);
        return ResponseEntity.ok(
                RestResponse.<GalleryResponseDto>builder()
                        .data(updated)
                        .message("Gallery item updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete gallery item",
            description = "Deletes a gallery image by ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the gallery item", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gallery item deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Gallery item not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<String>> deleteGallery(@PathVariable Long id)
    {
        log.info("Deleting gallery item with ID: {}", id);
        galleryService.deleteGallery(id);
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
            description = "Adds multiple image entries under a given session ID.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Gallery items created successfully"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<GalleryResponseDto>>> addBulkGalleryImages(
            @RequestBody BulkGalleryRequestDto dto
    )
    {
        log.info("Adding bulk gallery images for session ID: {}", dto.getSessionId());
        List<GalleryResponseDto> created = galleryService.addBulkGalleryImages(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<List<GalleryResponseDto>>builder()
                        .data(created)
                        .message("Bulk gallery images added")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }
    
    @Operation(
            summary = "Upload gallery image to Cloudinary",
            description = "Uploads an image file to Cloudinary and creates a gallery entry.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid file or request", content = @Content)
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<GalleryResponseDto>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("uploadedByType") String uploadedByType,
            @RequestParam("uploadedById") Long uploadedById,
            @RequestParam("uploadedByName") String uploadedByName,
            @RequestParam("sessionId") Long sessionId
    ) {
        try {
            log.info("Uploading gallery image for session ID: {} by {} ({})", sessionId, uploadedByName, uploadedByType);
            GalleryResponseDto created = galleryService.uploadGalleryImage(
                    file, title, description, uploadedByType, uploadedById, uploadedByName, sessionId
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    RestResponse.<GalleryResponseDto>builder()
                            .data(created)
                            .message("Image uploaded successfully")
                            .status(HttpStatus.CREATED.value())
                            .build()
            );
        } catch (IOException e) {
            log.error("Error uploading image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RestResponse.<GalleryResponseDto>builder()
                            .data(null)
                            .message("Failed to upload image: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build()
            );
        }
    }


}
