package com.java.slms.serviceImpl;


import com.java.slms.dto.ExamTypeRequestDto;
import com.java.slms.dto.ExamTypeResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ExamType;
import com.java.slms.model.School;
import com.java.slms.repository.ClassExamRepository;
import com.java.slms.repository.ExamRepository;
import com.java.slms.repository.ExamTypeRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.ScoreRepository;
import com.java.slms.service.ExamTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClassExamRepository classExamRepository;
    private final ExamRepository examRepository;
    private final ScoreRepository scoreRepository;

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
    @Transactional
    public void deleteExamType(Long schoolId, Long id)
    {
        log.info("Deleting exam type with id: {} for school: {}", id, schoolId);
        ExamType examType = examTypeRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found with id: " + id));
        
        // First, get all ClassExam entries associated with this ExamType
        List<com.java.slms.model.ClassExam> classExams = classExamRepository.findByExamType_Id(id);
        
        if (!classExams.isEmpty()) {
            log.info("Found {} ClassExam entries to delete for ExamType id: {}", classExams.size(), id);
            
            // For each ClassExam, delete associated Exams and their Scores
            for (com.java.slms.model.ClassExam classExam : classExams) {
                // Get all Exam entries for this ClassExam
                List<com.java.slms.model.Exam> exams = examRepository.findByClassExamIdAndSchoolId(classExam.getId(), schoolId);
                
                if (!exams.isEmpty()) {
                    log.info("Found {} Exam entries to delete for ClassExam id: {}", exams.size(), classExam.getId());
                    
                    // For each Exam, delete associated Scores
                    for (com.java.slms.model.Exam exam : exams) {
                        // Delete scores for this exam
                        List<com.java.slms.model.Score> scores = scoreRepository.findByExam_IdAndStudent_CurrentClass_Id(
                            exam.getId(), 
                            classExam.getClassEntity().getId()
                        );
                        if (!scores.isEmpty()) {
                            scoreRepository.deleteAll(scores);
                            log.info("Deleted {} Score entries for Exam id: {}", scores.size(), exam.getId());
                        }
                    }
                    
                    // Now delete all Exams for this ClassExam
                    examRepository.deleteAll(exams);
                    log.info("Deleted {} Exam entries for ClassExam id: {}", exams.size(), classExam.getId());
                }
            }
            
            // Now delete all ClassExam entries
            classExamRepository.deleteAll(classExams);
            log.info("Deleted {} ClassExam entries associated with ExamType id: {}", classExams.size(), id);
        }
        
        // Finally, delete the ExamType itself
        examTypeRepository.delete(examType);
        log.info("Successfully deleted ExamType with id: {}", id);
    }
}