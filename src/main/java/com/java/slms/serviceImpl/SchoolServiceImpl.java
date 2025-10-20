package com.java.slms.serviceImpl;

import com.java.slms.dto.SchoolRequestDto;
import com.java.slms.dto.SchoolResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.School;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolServiceImpl implements SchoolService
{
    private final SchoolRepository schoolRepository;
    private final ModelMapper modelMapper;

    @Override
    public SchoolResponseDto createSchool(SchoolRequestDto dto)
    {
        School school = modelMapper.map(dto, School.class);
        School saved = schoolRepository.save(school);
        return modelMapper.map(saved, SchoolResponseDto.class);
    }

    @Override
    public SchoolResponseDto getSchool(Long id)
    {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        return modelMapper.map(school, SchoolResponseDto.class);
    }

    @Override
    public List<SchoolResponseDto> getAllSchools()
    {
        return schoolRepository.findAll().stream()
                .map(school -> modelMapper.map(school, SchoolResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SchoolResponseDto updateSchool(Long id, SchoolRequestDto dto)
    {
        School existing = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        modelMapper.map(dto, existing);
        School updated = schoolRepository.save(existing);
        return modelMapper.map(updated, SchoolResponseDto.class);
    }

    @Override
    public void deleteSchool(Long id)
    {
        School existing = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        schoolRepository.deleteById(id);
    }
}
