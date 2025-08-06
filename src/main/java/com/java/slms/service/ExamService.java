package com.java.slms.service;

import com.java.slms.dto.ExamDto;

import java.util.List;

public interface ExamService
{
    ExamDto createExam(ExamDto examDto);

    List<ExamDto> getAllExams();

    ExamDto getExamByName(String name);

    List<ExamDto> getExamsByClassId(Long classId);

    ExamDto updateExam(String name, ExamDto examDto);

    void deleteExamByClass(Long examId, Long classId);
}
