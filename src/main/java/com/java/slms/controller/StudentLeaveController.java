package com.java.slms.controller;


import com.java.slms.dto.LeaveActionRequest;
import com.java.slms.dto.StudentLeaveRequestDTO;
import com.java.slms.dto.StudentLeaveResponse;
import com.java.slms.dto.TeacherDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentLeaveService;
import com.java.slms.service.TeacherService;
import com.java.slms.util.LeaveStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping("/api/leave")
@Tag(name = "Leave Controller", description = "APIs for managing student leave requests")
public class StudentLeaveController
{
    private final StudentLeaveService studentLeaveService;
    private final TeacherService teacherService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(
            summary = "Raise a leave request",
            description = "Allows a student to raise a leave request for a given date range. Automatically maps the active session and current class teacher.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave request submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Student or session not found", content = @Content)
            }
    )
    public ResponseEntity<RestResponse<Void>> requestLeave(@RequestBody StudentLeaveRequestDTO dto)
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        dto.setStudentPan(panNumber);

        studentLeaveService.createLeaveRequest(dto);

        RestResponse<Void> response = RestResponse.<Void>builder()
                .data(null)
                .message("Leave request submitted successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(
            summary = "Get student's leave records in active session",
            description = "Returns a list of leave requests raised by the currently logged-in student during the active academic session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of leave records"),
                    @ApiResponse(responseCode = "404", description = "Student or session not found", content = @Content)
            }
    )
    public ResponseEntity<RestResponse<List<StudentLeaveResponse>>> getMyLeaves()
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<StudentLeaveResponse> leaveList = studentLeaveService.getLeavesForLoggedInStudent(panNumber);

        RestResponse<List<StudentLeaveResponse>> response = RestResponse.<List<StudentLeaveResponse>>builder()
                .data(leaveList)
                .message("Leave records fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Take action on a student leave request",
            description = "Allows class teacher or admin to approve, reject, or cancel a leave request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid action or already processed"),
                    @ApiResponse(responseCode = "404", description = "Leave record not found")
            }
    )
    @PutMapping("/action/{leaveId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<?>> takeActionOnLeave(@PathVariable Long leaveId, @RequestBody LeaveActionRequest request)
    {
        String teacherEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TeacherDto teacher = teacherService.getTeacherByEmail(teacherEmail);

        studentLeaveService.takeActionOnLeave(leaveId, teacher.getId(), request);

        RestResponse<?> response = RestResponse.builder()
                .data(null)
                .message("Leave status updated successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @Operation(
            summary = "Get leave requests for current teacher",
            description = "Fetches all leave requests of students assigned to the currently logged-in teacher. Optionally filter by leave status.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave requests fetched successfully")
            }
    )
    public ResponseEntity<RestResponse<List<StudentLeaveResponse>>> getLeavesForTeacher(
            @RequestParam(required = false) LeaveStatus status
    )
    {
        String teacherEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TeacherDto teacher = teacherService.getTeacherByEmail(teacherEmail);

        List<StudentLeaveResponse> leaves = studentLeaveService.getLeavesForTeacher(teacher.getId(), status);

        RestResponse<List<StudentLeaveResponse>> response = RestResponse.<List<StudentLeaveResponse>>builder()
                .data(leaves)
                .message("Leave requests fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }


}
