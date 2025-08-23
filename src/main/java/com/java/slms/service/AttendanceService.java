package com.java.slms.service;

import com.java.slms.dto.*;
import com.java.slms.util.FeeMonth;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService
{
    void markTodaysAttendance(AttendanceDto attendanceDto);

    AttendanceUpdateResult updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate attendanceDate);

//    List<AttendanceInfoDto> getAllAttendanceByPanAndSessionId(String panNumber, Long sessionId);

    List<AttendanceInfoDto> getAllAttendanceByPanAndSessionId(String panNumber, Long sessionId, FeeMonth month);

    List<AttendanceByClassDto> getAttendanceByClassAndSession(Long classId, Long sessionId, FeeMonth month);
}
