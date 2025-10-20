package com.java.slms.service;

import com.java.slms.dto.ExamDto;
import com.java.slms.model.Exam;

import java.util.List;

public interface ExamService
{
    List<ExamDto> getExamsByClass(Long classId, Long schoolId);
    
    List<ExamDto> getExamsByClassExam(Long classExamId, Long schoolId);
    
    List<ExamDto> getExamsBySubject(Long subjectId, Long schoolId);
    
    ExamDto getExamById(Long id, Long schoolId);
    
    ExamDto createExam(ExamDto examDto, Long schoolId);
    
    ExamDto updateExam(Long id, ExamDto examDto, Long schoolId);
    
    void deleteExam(Long id, Long schoolId);
    
    List<ExamDto> getAllExams(Long schoolId);
    
    int syncExamsForClass(Long classId, Long schoolId);
}
