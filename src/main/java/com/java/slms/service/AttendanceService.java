package com.java.slms.service;

import com.java.slms.dto.*;
import com.java.slms.util.FeeMonth;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService
{
    void markTodaysAttendance(AttendanceDto attendanceDto, Long schoolId);

    AttendanceUpdateResult updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate date, Long schoolId);

    List<AttendanceInfoDto> getAllAttendanceByPanAndSessionId(String pan, Long sessionId, FeeMonth month, Long schoolId);

    List<AttendanceByClassDto> getAttendanceByClassAndSession(Long classId, Long sessionId, FeeMonth month, Long schoolId);
}
