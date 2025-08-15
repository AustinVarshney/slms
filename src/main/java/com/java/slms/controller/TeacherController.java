package com.java.slms.controller;

import com.java.slms.dto.TeacherDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.TeacherService;
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
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class TeacherController
{

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherDto>> createTeacher(@RequestBody TeacherDto teacherDto)
    {
        log.info("Creating teacher...");
        TeacherDto createdTeacher = teacherService.createTeacher(teacherDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TeacherDto>builder()
                        .data(createdTeacher)
                        .message("Teacher created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDto>> getTeacherById(@PathVariable Long id)
    {
        log.info("Fetching teacher with ID: {}", id);
        TeacherDto teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(
                ApiResponse.<TeacherDto>builder()
                        .data(teacher)
                        .message("Teacher retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDto>> getCurrentTeacher()
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("Fetching teacher with Email: {}", email);
        TeacherDto teacher = teacherService.getTeacherByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.<TeacherDto>builder()
                        .data(teacher)
                        .message("Teacher retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getAllTeachers()
    {
        log.info("Fetching all teachers...");
        List<TeacherDto> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(
                ApiResponse.<List<TeacherDto>>builder()
                        .data(teachers)
                        .message("All teachers retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getActiveTeachers()
    {
        log.info("Fetching all active teachers...");
        List<TeacherDto> teachers = teacherService.getActiveTeachers();
        return ResponseEntity.ok(
                ApiResponse.<List<TeacherDto>>builder()
                        .data(teachers)
                        .message("Active teachers retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDto>> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherDto teacherDto)
    {
        log.info("Updating teacher with ID: {}", id);
        TeacherDto updatedTeacher = teacherService.updateTeacher(id, teacherDto);
        return ResponseEntity.ok(
                ApiResponse.<TeacherDto>builder()
                        .data(updatedTeacher)
                        .message("Teacher updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id)
    {
        log.info("Hard deleting teacher with ID: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Teacher deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
