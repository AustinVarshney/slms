package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentQueryService;
import com.java.slms.service.TeacherService;
import com.java.slms.util.DayOfWeek;
import com.java.slms.util.QueryStatus;
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
public class StudentQueryController
{

    private final StudentQueryService studentQueryService;
    private final TeacherService teacherService;

    @PostMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<StudentQueryResponse>> raiseQueryToTeacher(
            @RequestBody StudentQueryRequest studentQueryRequest
    )
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StudentQueryResponse studentQueryResponse = studentQueryService.askQueryToTeacher(panNumber, studentQueryRequest);

        return ResponseEntity.ok(RestResponse.<StudentQueryResponse>builder()
                .data(studentQueryResponse)
                .message("Query Raised Successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<StudentQueryResponse>>> getAllQueriesByStudent(
            @RequestParam(value = "status", required = false) QueryStatus status
    )
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<StudentQueryResponse> queries = studentQueryService.getAllQueriesByStudent(panNumber, status);

        return ResponseEntity.ok(RestResponse.<List<StudentQueryResponse>>builder()
                .data(queries)
                .message("Student queries fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @PutMapping("/respond")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<StudentQueryResponse>> respondToQuery(
            @RequestBody TeacherResponseDto responseRequest
    )
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Teacher teacher = teacherService.getActiveTeacherByEmail(email);

        StudentQueryResponse response = studentQueryService.respondToQuery(teacher.getId(), responseRequest);

        return ResponseEntity.ok(RestResponse.<StudentQueryResponse>builder()
                .data(response)
                .message("Query responded successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/teacher/queries")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<StudentQueryResponse>>> getAllQueriesAssignedToTeacher(
            @RequestParam(value = "status", required = false) QueryStatus status
    )
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Teacher teacher = teacherService.getActiveTeacherByEmail(email);

        List<StudentQueryResponse> queries = studentQueryService.getAllQueriesAssignedToTeacher(teacher.getId(), status);

        return ResponseEntity.ok(RestResponse.<List<StudentQueryResponse>>builder()
                .data(queries)
                .message("Queries fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }


}
