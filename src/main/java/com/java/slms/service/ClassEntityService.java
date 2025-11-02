package com.java.slms.service;

import com.java.slms.dto.ClassInfoResponse;
import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;

import java.util.List;

public interface ClassEntityService
{
    ClassResponseDto addClass(Long schoolId, ClassRequestDto classRequestDto);

    List<ClassInfoResponse> getAllClassInActiveSession(Long schoolId);

    List<ClassInfoResponse> getAllClassesBySession(Long schoolId, Long sessionId);

    ClassInfoResponse getClassByClassIdAndSessionId(Long schoolId, Long classId);

    void deleteClassByIdAndSessionId(Long schoolId, Long id, Long sessionId);

    ClassInfoResponse updateClassNameById(Long schoolId, Long id, ClassRequestDto classRequestDto);

}
