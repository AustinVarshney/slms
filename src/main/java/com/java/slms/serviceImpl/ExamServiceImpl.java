package com.java.slms.serviceImpl;

import com.java.slms.dto.ExamDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Exam;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.ExamRepository;
import com.java.slms.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService
{

    private final ExamRepository examRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;

    @Override
    public ExamDto createExam(ExamDto examDto)
    {
        if (examRepository.existsByNameIgnoreCase(examDto.getName()))
        {
            throw new AlreadyExistException("Exam already exists with name: " + examDto.getName());
        }

        ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCase(examDto.getClassName());
        if (classEntity == null)
        {
            throw new ResourceNotFoundException("Class not found with name: " + examDto.getClassName());
        }

        Exam exam = modelMapper.map(examDto, Exam.class);
        exam.setClassEntity(classEntity);

        return modelMapper.map(examRepository.save(exam), ExamDto.class);
    }

    @Override
    public List<ExamDto> getAllExams()
    {
        return examRepository.findAll()
                .stream()
                .map(exam ->
                {
                    ExamDto dto = modelMapper.map(exam, ExamDto.class);
                    dto.setClassName(exam.getClassEntity().getClassName());
                    return dto;
                })
                .toList();
    }

    @Override
    public ExamDto getExamByName(String name)
    {
        return null;
    }

    @Override
    public List<ExamDto> getExamsByClassName(String className)
    {
        if (!classEntityRepository.existsByNameIgnoreCase(className))
        {
            throw new ResourceNotFoundException("Class not found with name: " + className);
        }

        return examRepository.findByClassEntity_ClassNameIgnoreCase(className)
                .stream()
                .map(exam ->
                {
                    ExamDto dto = modelMapper.map(exam, ExamDto.class);
                    dto.setClassName(exam.getClassEntity().getClassName());
                    return dto;
                })
                .toList();
    }

    @Override
    public ExamDto updateExam(String name, ExamDto examDto)
    {
        return null;
    }

    @Override
    public void deleteExam(String name)
    {
        Exam exam = examRepository.findByNameIgnoreCase(name);
        if (exam == null)
        {
            throw new ResourceNotFoundException("Exam not found with name: " + name);
        }

        examRepository.delete(exam);
    }
}
