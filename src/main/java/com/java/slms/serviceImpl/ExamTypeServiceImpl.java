package com.java.slms.serviceImpl;


import com.java.slms.dto.ExamTypeRequestDto;
import com.java.slms.dto.ExamTypeResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ExamType;
import com.java.slms.model.School;
import com.java.slms.repository.ExamTypeRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.ExamTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamTypeServiceImpl implements ExamTypeService
{

    private final ExamTypeRepository examTypeRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;

    @Override
    public ExamTypeResponseDto createExamType(Long schoolId, ExamTypeRequestDto dto)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        log.info("Creating exam type with name: {}", dto.getName());

        if (examTypeRepository.existsByNameIgnoreCaseAndSchoolId(dto.getName(), schoolId))
        {
            throw new WrongArgumentException("ExamType with name '" + dto.getName() + "' already exists");
        }

        // Map DTO to Entity using ModelMapper
        ExamType examType = modelMapper.map(dto, ExamType.class);
        examType.setSchool(school);
        examType = examTypeRepository.save(examType);
        log.info("Exam type created with id: {}", examType.getId());

        // Map saved entity back to Response DTO
        return modelMapper.map(examType, ExamTypeResponseDto.class);
    }

    @Override
    public ExamTypeResponseDto getExamTypeById(Long id, Long schoolId)
    {
        log.info("Fetching exam type with id: {}", id);
        ExamType examType = examTypeRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found with id: " + id));

        return modelMapper.map(examType, ExamTypeResponseDto.class);
    }

    @Override
    public List<ExamTypeResponseDto> getAllExamTypes(Long schoolId)
    {
        log.info("Fetching all exam types");
        List<ExamType> examTypes = examTypeRepository.findAllBySchoolId(schoolId);

        return examTypes.stream()
                .map(examType -> modelMapper.map(examType, ExamTypeResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ExamTypeResponseDto updateExamType(Long schoolId, Long id, ExamTypeRequestDto dto)
    {
        log.info("Updating exam type with id: {}", id);
        ExamType examType = examTypeRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found with id: " + id));

        if (!examType.getName().equals(dto.getName()) && examTypeRepository.existsByNameIgnoreCaseAndSchoolId(dto.getName(), schoolId))
        {
            throw new IllegalArgumentException("ExamType with name '" + dto.getName() + "' already exists");
        }

        // Update fields from DTO to entity
        modelMapper.map(dto, examType);

        examType = examTypeRepository.save(examType);
        log.info("Exam type updated with id: {}", examType.getId());

        return modelMapper.map(examType, ExamTypeResponseDto.class);
    }

    @Override
    public void deleteExamType(Long id, Long schoolId)
    {
        log.info("Deleting exam type with id: {}", id);
        ExamType examType = examTypeRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found with id: " + id));
        examTypeRepository.delete(examType);
        log.info("Exam type deleted with id: {}", id);
    }
}