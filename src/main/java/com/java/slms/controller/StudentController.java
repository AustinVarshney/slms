package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
@Tag(name = "Student Controller", description = "APIs for managing students")
public class StudentController
{
    private final StudentService studentService;

    @Operation(
            summary = "Get all students",
            description = "Retrieves all students in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getAllStudents()
    {
        List<StudentResponseDto> students = studentService.getAllStudent();
        RestResponse<List<StudentResponseDto>> response = RestResponse.<List<StudentResponseDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get active students",
            description = "Retrieves all active students.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Active students retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getActiveStudents()
    {
        List<StudentResponseDto> students = studentService.getActiveStudents();
        RestResponse<List<StudentResponseDto>> response = RestResponse.<List<StudentResponseDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get student by PAN number",
            description = "Fetches a student by their PAN number.",
            parameters = {
                    @Parameter(name = "panNumber", description = "PAN number of the student", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid PAN or student not found", content = @Content)
            }
    )
    @GetMapping("/{panNumber}")
    public ResponseEntity<RestResponse<StudentResponseDto>> getStudentByPAN(@PathVariable String panNumber)
    {
        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(studentService.getStudentByPAN(panNumber))
                .message("Student Fetched")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get current student details",
            description = "Fetches the currently logged in student's details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current student fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Unauthorized or student not found", content = @Content)
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<StudentResponseDto>> getCurrentStudent()
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StudentResponseDto student = studentService.getStudentByPAN(panNumber);

        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(student)
                .message("Current student fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update student information",
            description = "Updates information for active students identified by PAN number.",
            parameters = {
                    @Parameter(name = "panNumber", description = "PAN number of the student", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or student inactive", content = @Content)
            }
    )
    @PutMapping("/{panNumber}")
    public ResponseEntity<RestResponse<StudentResponseDto>> updateStudent(
            @PathVariable String panNumber,
            @RequestBody UpdateStudentInfo updateStudentInfo
    )
    {
        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(studentService.updateStudent(panNumber, updateStudentInfo))
                .message("Student updated successfully")
                .status(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get students present today",
            description = "Returns list of students marked present for today. Optional filter by class ID.",
            parameters = {
                    @Parameter(name = "classId", description = "Optional class ID to filter students")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Present students fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @GetMapping("/present-today")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<CurrentDayAttendance>> getPresentToday(
            @RequestParam(required = false) Long classId)
    {

        CurrentDayAttendance attendance = studentService.getStudentsPresentToday(Optional.ofNullable(classId));

        String message = (classId != null)
                ? "Students present today in class " + classId + ": " + attendance.getStudentAttendances().size()
                : "Students present today: " + attendance.getStudentAttendances().size();

        return ResponseEntity.ok(
                RestResponse.<CurrentDayAttendance>builder()
                        .data(attendance)
                        .message(message)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get students by class ID",
            description = "Fetch students for a given class by ID.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getStudentByClassId(@PathVariable Long classId)
    {
        List<StudentResponseDto> list = studentService.getStudentsByClassId(classId);
        return ResponseEntity.ok(
                RestResponse.<List<StudentResponseDto>>builder()
                        .data(list)
                        .message("Students in class " + classId + ": " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update students status",
            description = "Mark multiple students as graduate or inactive based on PAN numbers.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students marked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or student status", content = @Content)
            }
    )
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> updateStudentsStatus(
            @RequestBody UpdateStudentStatusRequest request)
    {

        studentService.markStudentsGraduateOrInActive(request.getPanNumbers(), request.getStatus());

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Students updated successfully with status: " + request.getStatus())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
