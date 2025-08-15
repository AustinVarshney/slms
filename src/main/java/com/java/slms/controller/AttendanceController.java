package com.java.slms.controller;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.dto.StudentDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN, 'ROLE_TEACHER'')")
    public ResponseEntity<ApiResponse<?>> markAttendance(@RequestBody AttendanceDto attendanceDto)
    {
        ApiResponse<?> response = ApiResponse.builder()
                .data(attendanceService.markAttendance(attendanceDto))
                .message("Attendance marked successfully ")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{date}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
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

}
