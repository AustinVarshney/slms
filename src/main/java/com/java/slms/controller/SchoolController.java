package com.java.slms.controller;

import com.java.slms.dto.SchoolRequestDto;
import com.java.slms.dto.SchoolResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Tag(name = "School Controller", description = "APIs for managing school data")
@Slf4j
public class SchoolController
{

    private final SchoolService schoolService;

    @Operation(
            summary = "Create a new school",
            responses = {
                    @ApiResponse(responseCode = "201", description = "School created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<SchoolResponseDto>> createSchool(@RequestBody SchoolRequestDto dto)
    {
        SchoolResponseDto created = schoolService.createSchool(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<SchoolResponseDto>builder()
                        .data(created)
                        .message("School created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(summary = "Retrieve school by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<SchoolResponseDto>> getSchool(@PathVariable Long id)
    {
        SchoolResponseDto school = schoolService.getSchool(id);
        return ResponseEntity.ok(
                RestResponse.<SchoolResponseDto>builder()
                        .data(school)
                        .message("School retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Retrieve all schools")
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<SchoolResponseDto>>> getAllSchools()
    {
        List<SchoolResponseDto> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(
                RestResponse.<List<SchoolResponseDto>>builder()
                        .data(schools)
                        .message("Schools retrieved")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Update school by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<SchoolResponseDto>> updateSchool(@PathVariable Long id, @RequestBody SchoolRequestDto dto)
    {
        SchoolResponseDto updated = schoolService.updateSchool(id, dto);
        return ResponseEntity.ok(
                RestResponse.<SchoolResponseDto>builder()
                        .data(updated)
                        .message("School updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Delete school by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteSchool(@PathVariable Long id)
    {
        schoolService.deleteSchool(id);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("School deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
