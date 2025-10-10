package com.java.slms.controller;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.model.Admin;
import com.java.slms.model.StaffLeaveRecord;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import com.java.slms.service.StaffLeaveService;
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
@RequestMapping("/api/staff-leaves")
@PreAuthorize("hasRole('ROLE_TEACHER')")
@Tag(name = "Staff Leave Controller", description = "APIs for staff leave requests")
public class StaffLeaveController
{
    private final StaffLeaveService staffLeaveService;
    private final TeacherService teacherService;
    private final AdminService adminService;

    @Operation(
            summary = "Raise a leave request",
            description = "Allows a teacher/staff to raise a leave request within their allowed quota",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave request submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient leave balance", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Teacher or session not found", content = @Content)
            }
    )
    @PostMapping("/request")
    public ResponseEntity<RestResponse<String>> raiseLeaveRequest(@RequestBody StaffLeaveRequestDto dto)
    {
        staffLeaveService.raiseLeaveRequest(dto);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Leave request submitted successfully")
                        .message("Leave request created")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get leave requests for the logged-in teacher in active session",
            description = "Returns leave records filtered by status (optional)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave records fetched"),
                    @ApiResponse(responseCode = "404", description = "Teacher or session not found", content = @Content)
            }
    )
    @GetMapping("/my-leaves")
    public ResponseEntity<RestResponse<List<StaffLeaveResponseDto>>> getMyLeaves(
            @RequestParam(name = "status", required = false) LeaveStatus status)
    {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherService.getActiveTeacherByEmail(email);

        List<StaffLeaveResponseDto> leaves = staffLeaveService.getMyLeaves(teacher.getId(), status);

        return ResponseEntity.ok(
                RestResponse.<List<StaffLeaveResponseDto>>builder()
                        .data(leaves)
                        .message("Total Leaves Found: " + leaves.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "View all teacher leave requests",
            description = "Allows admin to filter leaves by status, session, or teacher",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave records fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter inputs", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<StaffLeaveResponseDto>>> getAllLeavesForAdmin(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long teacherId)
    {

        List<StaffLeaveResponseDto> leaves = staffLeaveService.getAllLeavesForAdmin(status, sessionId, teacherId);

        return ResponseEntity.ok(
                RestResponse.<List<StaffLeaveResponseDto>>builder()
                        .data(leaves)
                        .message("Total Leaves Found: " + leaves.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Approve or reject a leave request",
            description = "Admin can update leave status to APPROVED or REJECTED",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Leave not found", content = @Content)
            }
    )
    @PutMapping("/{leaveId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<String>> updateLeaveStatus(
            @PathVariable Long leaveId,
            @RequestBody StaffLeaveStatusUpdateDto dto)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.getActiveAdminByEmail(email);

        staffLeaveService.updateLeaveStatus(leaveId, admin, dto);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Status updated to: " + dto.getStatus())
                        .message("Leave updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}