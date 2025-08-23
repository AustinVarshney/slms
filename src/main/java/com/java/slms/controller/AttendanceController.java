package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.Session;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.AttendanceService;
import com.java.slms.service.SessionService;
import com.java.slms.util.FeeMonth;
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
public class AttendanceController
{
    private final AttendanceService attendanceService;
    private final SessionService sessionService;

    @PostMapping()
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<?>> markAttendance(@RequestBody AttendanceDto attendanceDto)
    {
        attendanceService.markTodaysAttendance(attendanceDto);
        ApiResponse<?> response = ApiResponse.builder()
                .data(null)
                .message("Attendance marked successfully ")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{date}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceUpdateResult>> updateAttendanceForAdmin(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody AttendanceDto attendanceDto)
    {

        AttendanceUpdateResult attendanceUpdateResult = attendanceService.updateAttendanceForAdmin(attendanceDto, date);

        return ResponseEntity.ok(
                ApiResponse.<AttendanceUpdateResult>builder()
                        .data(attendanceUpdateResult)
                        .message("Attendance updated successfully for date: " + date)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("student/{pan}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceInfoDto>>> getAllAttendanceByPanAndSessionIdAndMonth(
            @PathVariable String pan,
            @PathVariable Long sessionId,
            @RequestParam(required = false) FeeMonth month)
    {

        List<AttendanceInfoDto> attendanceInfoList = attendanceService.getAllAttendanceByPanAndSessionId(pan, sessionId, month);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendanceInfoDto>>builder()
                        .data(attendanceInfoList)
                        .message("Attendance records fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );

    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceInfoDto>>> getAttendanceOfCurrentStudentByMonth(
            @RequestParam(required = false) FeeMonth month)
    {

        String panNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        SessionDto activeSession = sessionService.getCurrentSession();

        FeeMonth currentMonth = month;

        if (currentMonth == null && activeSession != null && activeSession.getStartDate() != null)
        {
            LocalDate now = LocalDate.now();
            if (!now.isBefore(activeSession.getStartDate()) && !now.isAfter(activeSession.getEndDate()))
            {
                currentMonth = FeeMonth.values()[now.getMonthValue() - 1];
            }
        }

        List<AttendanceInfoDto> records = attendanceService.getAllAttendanceByPanAndSessionId(
                panNumber, activeSession.getId(), currentMonth);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendanceInfoDto>>builder()
                        .data(records)
                        .message("Attendance records fetched for PAN: " + panNumber)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceByClassDto>>> getAttendanceByClassAndSessionAndMonth(
            @PathVariable Long classId,
            @PathVariable Long sessionId,
            @RequestParam(required = false) FeeMonth month)
    {

        List<AttendanceByClassDto> attendanceList = attendanceService.getAttendanceByClassAndSession(classId, sessionId, month);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendanceByClassDto>>builder()
                        .data(attendanceList)
                        .message("Attendance records fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


}
