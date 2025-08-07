package com.java.slms.controller;

import com.java.slms.dto.TeacherDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherController
{
    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherDto>> createTeacher(@RequestBody TeacherDto teacherDto)
    {
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
        TeacherDto teacher = teacherService.getTeacherById(id);
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
        List<TeacherDto> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(
                ApiResponse.<List<TeacherDto>>builder()
                        .data(teachers)
                        .message("All teachers retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDto>> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherDto teacherDto)
    {
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
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Teacher deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

//    /**
//     * Register a teacher to one or more subjects.
//     * <p>
//     * URL: POST /api/teachers/{teacherId}/subjects
//     * Body: List of subject IDs (JSON array)
//     */
//    @PostMapping("/{teacherId}/subjects")
//    public ResponseEntity<ApiResponse<Void>> registerTeacherToSubjects(
//            @PathVariable Long teacherId,
//            @RequestBody List<Long> subjectIds)
//    {
//
//        teacherService.registerTeacherToSubjects(teacherId, subjectIds);
//
//        return ResponseEntity.ok(
//                ApiResponse.<Void>builder()
//                        .message("Teacher registered to subjects successfully")
//                        .status(HttpStatus.OK.value())
//                        .build()
//        );
//    }
//
//    /**
//     * Register a teacher to classes based on subjects they are registered for.
//     * <p>
//     * URL: POST /api/teachers/{teacherId}/classes/register-by-subjects
//     * Body: List of subject IDs (JSON array)
//     */
//    @PostMapping("/{teacherId}/classes/register-by-subjects")
//    public ResponseEntity<ApiResponse<Void>> registerTeacherToClassesBasedOnSubjects(
//            @PathVariable Long teacherId,
//            @RequestBody List<Long> subjectIds)
//    {
//
//        teacherService.registerTeacherToClassesBasedOnSubjects(teacherId, subjectIds);
//
//        return ResponseEntity.ok(
//                ApiResponse.<Void>builder()
//                        .message("Teacher registered to classes based on subjects successfully")
//                        .status(HttpStatus.OK.value())
//                        .build()
//        );
//    }


}
