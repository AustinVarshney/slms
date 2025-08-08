package com.java.slms.service;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.util.DayOfWeek;

import java.util.List;

public interface TimeTableService
{
    public TimetableResponseDTO createTimetable(TimetableRequestDTO dto);

    List<TimetableResponseDTO> getTimetableByClassId(Long classId);

    List<TimetableResponseDTO> getTimetableByTeacherId(Long teacherId);

    List<TimetableResponseDTO> getTimetableByClassAndDay(Long classId, DayOfWeek day);

    TimetableResponseDTO updateTimetable(Long id, TimetableRequestDTO dto);

    void deleteTimetable(Long id);
}
