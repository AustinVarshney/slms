package com.java.slms.controller;

import com.java.slms.dto.StudentDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController
{
    private final AttendanceService attendanceService;

    @PostMapping("/{panNumber}")
    public ResponseEntity<ApiResponse<?>> markAttendance(@PathVariable String panNumber, @RequestParam boolean isPresent) {
        attendanceService.markAttendance(panNumber, isPresent);

        ApiResponse<?> response = ApiResponse.builder()
                .message("Attendance marked successfully for student with PAN: " + panNumber)
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }
}
