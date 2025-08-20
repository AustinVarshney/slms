package com.java.slms.service;

import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;

import java.util.List;

public interface ClassEntityService
{
    ClassResponseDto addClass(ClassRequestDto classRequestDto);

    List<ClassResponseDto> getAllClass();

    ClassResponseDto getClassByClassIdAndSessionId(Long classId, Long sessionId);

    void deleteClassByIdAndSessionId(Long id, Long sessionId);

    ClassResponseDto updateClassNameById(Long id, ClassRequestDto classRequestDto);

}
