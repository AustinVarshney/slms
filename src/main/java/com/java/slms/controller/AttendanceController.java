package com.java.slms.controller;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.dto.AttendenceResponse;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.AttendanceService;
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

    @PostMapping()
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<?>> markAttendance(@RequestBody AttendanceDto attendanceDto)
    {
        ApiResponse<?> response = ApiResponse.builder()
                .data(attendanceService.markTodaysAttendance(attendanceDto))
                .message("Attendance marked successfully ")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{date}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDto>> updateAttendanceForAdmin(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody AttendanceDto attendanceDto)
    {

        AttendanceDto updatedAttendance = attendanceService.updateAttendanceForAdmin(attendanceDto, date);

        return ResponseEntity.ok(
                ApiResponse.<AttendanceDto>builder()
                        .data(updatedAttendance)
                        .message("Attendance updated successfully for date: " + date)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{pan}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendenceResponse>>> getAllAttendance(@PathVariable String pan)
    {
        List<AttendenceResponse> records = attendanceService.getAllAttendanceByPan(pan);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendenceResponse>>builder()
                        .data(records)
                        .message("Attendance records fetched for PAN: " + pan)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendenceResponse>>> getAttendanceOfCurrentStudent()
    {

        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<AttendenceResponse> records = attendanceService.getAllAttendanceByPan(panNumber);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendenceResponse>>builder()
                        .data(records)
                        .message("Attendance records fetched for PAN: " + panNumber)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
