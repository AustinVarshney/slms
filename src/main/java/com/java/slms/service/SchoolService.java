package com.java.slms.service;

import com.java.slms.dto.SchoolRequestDto;
import com.java.slms.dto.SchoolResponseDto;

import java.util.List;

public interface SchoolService
{
    SchoolResponseDto createSchool(SchoolRequestDto requestDto);

    SchoolResponseDto getSchool(Long id);

    List<SchoolResponseDto> getAllSchools();

    SchoolResponseDto updateSchool(Long id, SchoolRequestDto requestDto);

    void deleteSchool(Long id);
}
