package com.java.slms.service;

import com.java.slms.dto.ExamTypeRequestDto;
import com.java.slms.dto.ExamTypeResponseDto;

import java.util.List;

public interface ExamTypeService
{
    ExamTypeResponseDto createExamType(Long schoolId, ExamTypeRequestDto dto);

    ExamTypeResponseDto getExamTypeById(Long schoolId, Long id);

    List<ExamTypeResponseDto> getAllExamTypes(Long schoolId);

    ExamTypeResponseDto updateExamType(Long schoolId, Long id, ExamTypeRequestDto dto);

    void deleteExamType(Long schoolId, Long id);
}
