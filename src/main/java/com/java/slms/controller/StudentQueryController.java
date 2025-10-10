package com.java.slms.controller;

import com.java.slms.dto.StudentQueryRequest;
import com.java.slms.dto.StudentQueryResponse;
import com.java.slms.dto.TeacherResponseDto;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentQueryService;
import com.java.slms.service.TeacherService;
import com.java.slms.util.QueryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/student-query")
@Tag(name = "Student Query Controller", description = "APIs for students to raise queries and teachers to respond")
public class StudentQueryController
{

    private final StudentQueryService studentQueryService;
    private final TeacherService teacherService;

    @Operation(
            summary = "Raise a query to a teacher",
            description = "Allows a student to raise a query to the assigned teacher.",
            requestBody = @RequestBody(
                    description = "Query details submitted by student",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StudentQueryRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Query raised successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or teacher not found")
            }
    )
    @PostMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<StudentQueryResponse>> raiseQueryToTeacher(
            @org.springframework.web.bind.annotation.RequestBody StudentQueryRequest studentQueryRequest
    )
    {
        String panNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        StudentQueryResponse studentQueryResponse = studentQueryService.askQueryToTeacher(panNumber, studentQueryRequest);

        return ResponseEntity.ok(RestResponse.<StudentQueryResponse>builder()
                .data(studentQueryResponse)
                .message("Query Raised Successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get all queries by the logged-in student",
            description = "Fetches all queries raised by the authenticated student, optionally filtered by status.",
            parameters = {
                    @Parameter(name = "status", description = "Filter queries by their status (e.g., PENDING, RESOLVED)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Queries fetched successfully")
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<StudentQueryResponse>>> getAllQueriesByStudent(
            @RequestParam(value = "status", required = false) QueryStatus status
    )
    {
        String panNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        List<StudentQueryResponse> queries = studentQueryService.getAllQueriesByStudent(panNumber, status);

        return ResponseEntity.ok(RestResponse.<List<StudentQueryResponse>>builder()
                .data(queries)
                .message("Student queries fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Respond to a student query",
            description = "Allows a teacher to respond to a specific query raised by a student.",
            requestBody = @RequestBody(
                    description = "Response details from the teacher",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Query responded successfully"),
                    @ApiResponse(responseCode = "404", description = "Query not found or unauthorized")
            }
    )
    @PutMapping("/respond")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<StudentQueryResponse>> respondToQuery(
            @org.springframework.web.bind.annotation.RequestBody TeacherResponseDto responseRequest
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherService.getActiveTeacherByEmail(email);
        StudentQueryResponse response = studentQueryService.respondToQuery(teacher.getId(), responseRequest);

        return ResponseEntity.ok(RestResponse.<StudentQueryResponse>builder()
                .data(response)
                .message("Query responded successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get all queries assigned to the logged-in teacher",
            description = "Fetches all student queries assigned to the authenticated teacher, optionally filtered by status.",
            parameters = {
                    @Parameter(name = "status", description = "Filter queries by status", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Queries fetched successfully")
            }
    )
    @GetMapping("/teacher/queries")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<StudentQueryResponse>>> getAllQueriesAssignedToTeacher(
            @RequestParam(value = "status", required = false) QueryStatus status
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherService.getActiveTeacherByEmail(email);
        List<StudentQueryResponse> queries = studentQueryService.getAllQueriesAssignedToTeacher(teacher.getId(), status);

        return ResponseEntity.ok(RestResponse.<List<StudentQueryResponse>>builder()
                .data(queries)
                .message("Queries fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }
}
