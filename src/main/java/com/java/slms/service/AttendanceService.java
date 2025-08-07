package com.java.slms.service;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.dto.StudentDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService
{
    AttendanceDto markAttendance(AttendanceDto attendanceDto);

    AttendanceDto updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate attendanceDate);
}
