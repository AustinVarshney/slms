package com.java.slms.controller;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.model.Admin;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import com.java.slms.service.StaffLeaveRecordService;
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
    private final StaffLeaveRecordService staffLeaveService;
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
    public ResponseEntity<RestResponse<String>> raiseLeaveRequest(
            @RequestBody StaffLeaveRequestDto dto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        staffLeaveService.raiseLeaveRequest(dto, schoolId, email);

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
            @RequestParam(name = "status", required = false) LeaveStatus status,
            @RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<StaffLeaveResponseDto> leaves = staffLeaveService.getMyLeaves(email, status, schoolId);

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
            @RequestParam(required = false) Long staffId,
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<StaffLeaveResponseDto> leaves = staffLeaveService.getAllLeavesForAdmin(status, sessionId, staffId, schoolId);

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
            @RequestBody StaffLeaveStatusUpdateDto dto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.getAdminInfo(email, schoolId);

        staffLeaveService.updateLeaveStatus(leaveId, admin, dto, schoolId);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Status updated to: " + dto.getStatus())
                        .message("Leave updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
