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
        // Check if the exam already exists for the class using classId
        if (examRepository.existsByNameIgnoreCaseAndClassEntity_Id(examDto.getName(), examDto.getClassId()))
        {
            throw new AlreadyExistException("Exam '" + examDto.getName() + "' already exists for class with ID: " + examDto.getClassId() + ".");
        }

        // Fetch the ClassEntity by classId
        ClassEntity classEntity = classEntityRepository.findById(examDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + examDto.getClassId()));

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
    public List<ExamDto> getExamsByClassId(Long classId)
    {
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class not found with ID: " + classId);
        }

        // Find exams associated with this classId
        return examRepository.findByClassEntity_Id(classId)
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
    public void deleteExamByClass(Long examId, Long classId)
    {
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class not found with ID: " + classId);
        }

        if (!examRepository.existsById(examId))
        {
            throw new ResourceNotFoundException("Exam not found with ID: " + examId);
        }

        Exam exam = examRepository.findByIdAndClassEntityId(examId, classId);
        if (exam == null)
        {
            throw new ResourceNotFoundException("Exam with ID: " + examId +
                    " not found for class with ID: " + classId);
        }

        examRepository.delete(exam);
    }

}
