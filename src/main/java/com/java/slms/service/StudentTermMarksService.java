package com.java.slms.service;

import com.java.slms.dto.CreateMarksDto;
import com.java.slms.dto.StudentExamSummaryDto;
import com.java.slms.dto.StudentMarksResponseDto;

import java.util.List;

public interface StudentTermMarksService
{
    void addMarksOfStudentsByExamTypeAndClassIdInCurrentSession(CreateMarksDto marksDto, Long schoolId, Long subjectId, Long classId);

    StudentMarksResponseDto getStudentMarks(String panNumber);

    List<StudentExamSummaryDto> getExamSummaryByPanNumber(String panNumber);
}
