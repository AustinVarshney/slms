package com.java.slms.service;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.util.DayOfWeek;

import java.util.List;

public interface TimeTableService
{
    TimetableResponseDTO createTimetable(TimetableRequestDTO dto, Long schoolId);

    List<TimetableResponseDTO> getTimetableByTeacherIdInCurrentSession(Long teacherId, Long schoolId);

    List<TimetableResponseDTO> getTimetableByClassAndOptionalDay(Long classId, DayOfWeek day, Long schoolId);

    TimetableResponseDTO updateTimetable(Long id, TimetableRequestDTO dto, Long schoolId);

    void deleteTimetable(Long id, Long schoolId);
}
