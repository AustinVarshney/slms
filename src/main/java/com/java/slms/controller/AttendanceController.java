package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AttendanceService;
import com.java.slms.service.SessionService;
import com.java.slms.util.FeeMonth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Controller", description = "APIs for managing attendance records")
public class AttendanceController
{
    private final AttendanceService attendanceService;
    private final SessionService sessionService;

    @Operation(
            summary = "Mark today's attendance",
            description = "Allows admin or teacher to mark attendance for students on the current date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance marked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid attendance data or missing class ID", content = @Content)
            }
    )
    @PostMapping()
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<?>> markAttendance(
            @RequestBody AttendanceDto attendanceDto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        attendanceService.markTodaysAttendance(attendanceDto, schoolId);
        RestResponse<?> response = RestResponse.builder()
                .data(null)
                .message("Attendance marked successfully ")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update attendance for a specific date",
            description = "Allows admin to update attendance records for a given date.",
            parameters = {
                    @Parameter(name = "date", description = "Date of attendance in YYYY-MM-DD format", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or date out of session range", content = @Content)
            }
    )
    @PutMapping("/{date}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<AttendanceUpdateResult>> updateAttendanceForAdmin(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody AttendanceDto attendanceDto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        AttendanceUpdateResult attendanceUpdateResult = attendanceService.updateAttendanceForAdmin(attendanceDto, date, schoolId);

        return ResponseEntity.ok(
                RestResponse.<AttendanceUpdateResult>builder()
                        .data(attendanceUpdateResult)
                        .message("Attendance updated successfully for date: " + date)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get attendance records by student PAN, session, and optionally by month",
            description = "Fetch attendance records for a student in a specified session and optional month filter.",
            parameters = {
                    @Parameter(name = "pan", description = "PAN number of the student", required = true),
                    @Parameter(name = "sessionId", description = "ID of the session", required = true),
                    @Parameter(name = "month", description = "Month for attendance filter (optional)"),
                    @Parameter(name = "schoolId", description = "School ID", required = true, in = ParameterIn.DEFAULT)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance records fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content)
            }
    )
    @GetMapping("student/{pan}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<AttendanceInfoDto>>> getAllAttendanceByPanAndSessionIdAndMonth(
            @PathVariable String pan,
            @PathVariable Long sessionId,
            @RequestParam(required = false) FeeMonth month,
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<AttendanceInfoDto> attendanceInfoList = attendanceService.getAllAttendanceByPanAndSessionId(pan, sessionId, month, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<AttendanceInfoDto>>builder()
                        .data(attendanceInfoList)
                        .message("Attendance records fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );

    }

    @Operation(
            summary = "Get attendance records of the current student by month",
            description = "Fetch attendance records for the logged-in student filtered by optional month.",
            parameters = {
                    @Parameter(name = "month", description = "Month for attendance filter (optional)"),
                    @Parameter(name = "schoolId", description = "School ID", required = true, in = ParameterIn.DEFAULT)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance records fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content)
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<AttendanceInfoDto>>> getAttendanceOfCurrentStudentByMonth(
            @RequestParam(required = false) FeeMonth month,
            @RequestAttribute("schoolId") Long schoolId)
    {

        String panNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        SessionDto activeSession = sessionService.getCurrentSession(schoolId);

        FeeMonth currentMonth = month;

        if (currentMonth == null && activeSession != null && activeSession.getStartDate() != null)
        {
            // Determine current month only if within session range
            LocalDate now = LocalDate.now();
            if (!now.isBefore(activeSession.getStartDate()) && !now.isAfter(activeSession.getEndDate()))
            {
                currentMonth = FeeMonth.values()[now.getMonthValue() - 1];
            }
        }

        List<AttendanceInfoDto> records = attendanceService.getAllAttendanceByPanAndSessionId(
                panNumber, activeSession.getId(), currentMonth, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<AttendanceInfoDto>>builder()
                        .data(records)
                        .message("Attendance records fetched for PAN: " + panNumber)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get attendance records by class, session, and optional month",
            description = "Fetch attendance records for a class during a session filtered by optional month.",
            parameters = {
                    @Parameter(name = "classId", description = "Class ID", required = true),
                    @Parameter(name = "sessionId", description = "Session ID", required = true),
                    @Parameter(name = "month", description = "Month for attendance filter (optional)"),
                    @Parameter(name = "schoolId", description = "School ID", required = true, in = ParameterIn.DEFAULT)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance records fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content)
            }
    )
    @GetMapping("/class/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<AttendanceByClassDto>>> getAttendanceByClassAndSessionAndMonth(
            @PathVariable Long classId,
            @PathVariable Long sessionId,
            @RequestParam(required = false) FeeMonth month,
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<AttendanceByClassDto> attendanceList = attendanceService.getAttendanceByClassAndSession(classId, sessionId, month, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<AttendanceByClassDto>>builder()
                        .data(attendanceList)
                        .message("Attendance records fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @Operation(
            summary = "Get attendance records by class and specific date",
            description = "Fetch attendance records for a class on a specific date.",
            parameters = {
                    @Parameter(name = "classId", description = "Class ID", required = true),
                    @Parameter(name = "date", description = "Date in YYYY-MM-DD format", required = true),
                    @Parameter(name = "schoolId", description = "School ID", required = true, in = ParameterIn.DEFAULT)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attendance records fetched successfully"),
                    @ApiResponse(responseCode = "404", description = "No attendance found for the given date", content = @Content)
            }
    )
    @GetMapping("/class/{classId}/date/{date}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<AttendanceDto>> getAttendanceByClassAndDate(
            @PathVariable Long classId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("schoolId") Long schoolId)
    {
        AttendanceDto attendance = attendanceService.getAttendanceByClassAndDate(classId, date, schoolId);
        
        if (attendance == null)
        {
            return ResponseEntity.ok(
                    RestResponse.<AttendanceDto>builder()
                            .data(null)
                            .message("No attendance records found for the given date")
                            .status(HttpStatus.OK.value())
                            .build()
            );
        }

        return ResponseEntity.ok(
                RestResponse.<AttendanceDto>builder()
                        .data(attendance)
                        .message("Attendance records fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
