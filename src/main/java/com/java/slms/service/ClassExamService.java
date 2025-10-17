package com.java.slms.service;

import com.java.slms.dto.ClassExamBulkRequestDto;
import com.java.slms.dto.ClassExamResponseDto;
import com.java.slms.dto.ClassExamUpdateDto;

import java.util.List;

public interface ClassExamService
{
    void assignExamTypeToMultipleClasses(ClassExamBulkRequestDto dto, Long schoolId);

    List<ClassExamResponseDto> getExamsByClass(Long classId, Long schoolId);

    void updateClassExam(Long classId, Long examTypeId, ClassExamUpdateDto dto, Long schoolId);

    void deleteClassExam(Long classId, Long examTypeId, Long schoolId);

}
