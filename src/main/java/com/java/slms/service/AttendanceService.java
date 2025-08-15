package com.java.slms.service;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.dto.AttendenceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService
{
    AttendanceDto markTodaysAttendance(AttendanceDto attendanceDto);

    AttendanceDto updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate attendanceDate);

    List<AttendenceResponse> getAllAttendanceByPan(String panNumber);
}
