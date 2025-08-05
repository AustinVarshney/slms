package com.java.slms.service;

import com.java.slms.dto.StudentDto;

import java.util.List;

public interface AttendanceService
{
    void markAttendance(String studentPanNumber, boolean isPresent);
}
