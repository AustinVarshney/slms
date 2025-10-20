package com.java.slms.controller;

import com.java.slms.dto.SessionDto;
import com.java.slms.dto.StaffLeaveAllowanceDto;
import com.java.slms.model.Staff;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.SessionService;
import com.java.slms.service.StaffLeaveAllowanceService;
import com.java.slms.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/staff-leave-allowances")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Staff Leave Allowance", description = "Manage leave allowance for staff members")
public class StaffLeaveAllowanceController
{

    private final StaffLeaveAllowanceService allowanceService;
    private final SessionService sessionService;
    private final StaffService staffService;

    @Operation(
            summary = "Create new leave allowance for staff",
            description = "Creates a leave allowance entry for a staff in a session. Fails if already exists.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allowance created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid data or duplicate allowance", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Staff or Session not found", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<RestResponse<String>> createAllowance(
            @RequestBody StaffLeaveAllowanceDto dto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        allowanceService.createLeaveAllowanceInCurrentSession(dto, schoolId);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Leave allowance created successfully")
                        .message("Created")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update existing staff leave allowance",
            description = "Updates an existing leave allowance by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allowance updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Allowance, staff, or session not found", content = @Content)
            }
    )
    @PutMapping("/{allowanceId}")
    public ResponseEntity<RestResponse<String>> updateAllowance(
            @PathVariable Long allowanceId,
            @RequestBody StaffLeaveAllowanceDto dto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        allowanceService.updateLeaveAllowance(allowanceId, dto, schoolId);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Leave allowance updated successfully")
                        .message("Updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get leave allowance for specific staff and session",
            description = "Fetches a single allowance entry using staffId and sessionId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allowance found",
                            content = @Content(schema = @Schema(implementation = StaffLeaveAllowanceDto.class))),
                    @ApiResponse(responseCode = "404", description = "Allowance not found", content = @Content)
            }
    )
    @GetMapping("/staff/{staffId}/session/{sessionId}")
    public ResponseEntity<RestResponse<StaffLeaveAllowanceDto>> getAllowance(
            @PathVariable Long staffId,
            @PathVariable Long sessionId,
            @RequestAttribute("schoolId") Long schoolId)
    {

        StaffLeaveAllowanceDto dto = allowanceService.getAllowance(staffId, sessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<StaffLeaveAllowanceDto>builder()
                        .data(dto)
                        .message("Allowance fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all leave allowances for a session",
            description = "Returns all staff leave allowances assigned for the given session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allowances found"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<RestResponse<List<StaffLeaveAllowanceDto>>> getAllowancesBySession(
            @PathVariable Long sessionId,
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<StaffLeaveAllowanceDto> allowances = allowanceService.getAllowancesForSession(sessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<StaffLeaveAllowanceDto>>builder()
                        .data(allowances)
                        .message("Total: " + allowances.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all leave allowances for a session",
            description = "Returns all staff leave allowances assigned for the given session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allowances found"),
                    @ApiResponse(responseCode = "404", description = "Session not found", content = @Content)
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<StaffLeaveAllowanceDto>> getMyLeaveAllowanceInCurrentSession(
            @RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        SessionDto currentSession = sessionService.getCurrentSession(schoolId);
        Staff staff = staffService.getStaffByEmailAndSchoolId(email, schoolId);

        StaffLeaveAllowanceDto allowances = allowanceService.getAllowance(staff.getId(), currentSession.getId(), schoolId);

        return ResponseEntity.ok(
                RestResponse.<StaffLeaveAllowanceDto>builder()
                        .data(allowances)
                        .message("Leaves fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
