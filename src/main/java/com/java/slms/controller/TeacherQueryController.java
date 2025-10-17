package com.java.slms.controller;

import com.java.slms.dto.AdminResponseDto;
import com.java.slms.dto.TeacherQueryRequest;
import com.java.slms.dto.TeacherQueryResponse;
import com.java.slms.model.Admin;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import com.java.slms.service.TeacherQueryService;
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
@RequestMapping("api/teacher-query")
@Tag(name = "Teacher Query Controller", description = "APIs for teachers to raise queries to admins and for admins to respond")
public class TeacherQueryController
{

    private final TeacherQueryService teacherQueryService;
    private final TeacherService teacherService;
    private final AdminService adminService;

    @Operation(
            summary = "Raise a query to admin",
            description = "Allows a teacher to raise a query or concern to the admin.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Details of the query being raised",
                    content = @Content(schema = @Schema(implementation = TeacherQueryRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Query raised successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
            }
    )
    @PostMapping("/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<TeacherQueryResponse>> raiseQueryToAdmin(
            @org.springframework.web.bind.annotation.RequestBody TeacherQueryRequest request,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        TeacherQueryResponse response = teacherQueryService.askQueryToAdmin(email, request, schoolId);

        return ResponseEntity.ok(RestResponse.<TeacherQueryResponse>builder()
                .data(response)
                .message("Query raised to Admin successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get all queries raised by the logged-in teacher",
            description = "Returns a list of all queries raised by the currently logged-in teacher, optionally filtered by status.",
            parameters = {
                    @Parameter(name = "status", description = "Filter by query status (e.g., PENDING, RESOLVED)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Teacher queries fetched successfully")
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<TeacherQueryResponse>>> getMyQueries(
            @RequestParam(value = "status", required = false) QueryStatus status,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<TeacherQueryResponse> queries = teacherQueryService.getAllQueriesByTeacher(email, status, schoolId);

        return ResponseEntity.ok(RestResponse.<List<TeacherQueryResponse>>builder()
                .data(queries)
                .message("Fetched teacher queries")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Respond to a teacher's query",
            description = "Allows an admin to respond to a query submitted by a teacher.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Response content provided by admin",
                    content = @Content(schema = @Schema(implementation = AdminResponseDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Query responded successfully"),
                    @ApiResponse(responseCode = "404", description = "Query not found", content = @Content)
            }
    )
    @PutMapping("/respond")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<TeacherQueryResponse>> respondToQuery(
            @org.springframework.web.bind.annotation.RequestBody AdminResponseDto responseRequest,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.getAdminInfo(email, schoolId);

        TeacherQueryResponse response = teacherQueryService.respondToTeacherQuery(admin, responseRequest, schoolId);

        return ResponseEntity.ok(RestResponse.<TeacherQueryResponse>builder()
                .data(response)
                .message("Query responded successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get all teacher queries assigned to admin",
            description = "Returns a list of all queries raised by teachers that are assigned to the logged-in admin.",
            parameters = {
                    @Parameter(name = "status", description = "Filter by query status (e.g., PENDING, RESOLVED)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Queries fetched successfully")
            }
    )
    @GetMapping("/admin/queries")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<TeacherQueryResponse>>> getAllQueriesAssignedToAdmin(
            @RequestParam(value = "status", required = false) QueryStatus status,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.getAdminInfo(email, schoolId);

        List<TeacherQueryResponse> queries = teacherQueryService.getAllQueriesAssignedToAdmin(admin, status, schoolId);

        return ResponseEntity.ok(RestResponse.<List<TeacherQueryResponse>>builder()
                .data(queries)
                .message("Queries fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }
}
