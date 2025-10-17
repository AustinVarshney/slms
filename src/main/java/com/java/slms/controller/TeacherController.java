package com.java.slms.controller;

import com.java.slms.dto.TeacherDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Teacher Controller", description = "APIs for managing teachers")
public class TeacherController
{

    private final TeacherService teacherService;

    //    @Operation(
//            summary = "Create a teacher",
//            description = "Creates a new teacher record.",
//            responses = {
//                    @ApiResponse(responseCode = "201", description = "Teacher created successfully"),
//                    @ApiResponse(responseCode = "400", description = "Invalid request or teacher already exists", content = @Content)
//            }
//    )
//    @PostMapping
//    public ResponseEntity<RestResponse<TeacherDto>> createTeacher(@RequestBody TeacherDto teacherDto)
//    {
//        log.info("Creating teacher...");
//        TeacherDto createdTeacher = teacherService.createTeacher(teacherDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//                RestResponse.<TeacherDto>builder()
//                        .data(createdTeacher)
//                        .message("Teacher created successfully")
//                        .status(HttpStatus.CREATED.value())
//                        .build()
//        );
//    }
    @Operation(
            summary = "Get teacher by ID",
            description = "Retrieves a teacher by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Teacher retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid ID or teacher not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<TeacherDto>> getTeacherById(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {
        log.info("Fetching teacher with ID: {} for school ID: {}", id, schoolId);
        TeacherDto teacher = teacherService.getTeacherById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<TeacherDto>builder()
                        .data(teacher)
                        .message("Teacher retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get current logged-in teacher",
            description = "Retrieves the currently authenticated teacher by email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Teacher retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Unauthorized or teacher not found", content = @Content)
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<TeacherDto>> getCurrentTeacher(@RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("Fetching teacher with Email: {} for school ID: {}", email, schoolId);
        TeacherDto teacher = teacherService.getTeacherByEmail(email, schoolId);
        return ResponseEntity.ok(
                RestResponse.<TeacherDto>builder()
                        .data(teacher)
                        .message("Teacher retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all teachers",
            description = "Retrieves all teachers for the school.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All teachers retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<TeacherDto>>> getAllTeachers(
            @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Fetching all teachers for school ID: {}", schoolId);
        List<TeacherDto> teachers = teacherService.getAllTeachers(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<TeacherDto>>builder()
                        .data(teachers)
                        .message("All teachers retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get active teachers",
            description = "Retrieves all active teachers for the school.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Active teachers retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<TeacherDto>>> getActiveTeachers(
            @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Fetching all active teachers for school ID: {}", schoolId);
        List<TeacherDto> teachers = teacherService.getActiveTeachers(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<TeacherDto>>builder()
                        .data(teachers)
                        .message("Active teachers retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Deactivate teacher",
            description = "Marks a teacher as inactive by ID for the given school.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Teacher deactivated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid ID or teacher already inactive", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> inActiveTeacher(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {
        log.info("Inactivating teacher with ID: {} for school ID: {}", id, schoolId);
        teacherService.inActiveTeacher(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Teacher deactivated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
