package com.java.slms.service;

import com.java.slms.dto.ClassInfoResponse;
import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;

import java.util.List;

public interface ClassEntityService
{
    ClassResponseDto addClass(ClassRequestDto classRequestDto);

    List<ClassInfoResponse> getAllClassInActiveSession();

    ClassInfoResponse getClassByClassIdAndSessionId(Long classId, Long sessionId);

    void deleteClassByIdAndSessionId(Long id, Long sessionId);

    ClassInfoResponse updateClassNameById(Long id, ClassRequestDto classRequestDto);

}
