package com.java.slms.service.impl;

import com.java.slms.dto.ExamDto;
import com.java.slms.model.ClassExam;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Exam;
import com.java.slms.model.Subject;
import com.java.slms.repository.ClassExamRepository;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.ExamRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamServiceImpl implements ExamService
{
    private final ExamRepository examRepository;
    private final ClassExamRepository classExamRepository;
    private final ClassEntityRepository classEntityRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsByClass(Long classId, Long schoolId)
    {
        List<Exam> exams = examRepository.findByClassIdAndSchoolId(classId, schoolId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsByClassExam(Long classExamId, Long schoolId)
    {
        List<Exam> exams = examRepository.findByClassExamIdAndSchoolId(classExamId, schoolId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsBySubject(Long subjectId, Long schoolId)
    {
        List<Exam> exams = examRepository.findBySubjectIdAndSchoolId(subjectId, schoolId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto getExamById(Long id, Long schoolId)
    {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
        
        if (!exam.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to exam");
        }
        
        return convertToDTO(exam);
    }

    @Override
    public ExamDto createExam(ExamDto examDto, Long schoolId)
    {
        ClassExam classExam = classExamRepository.findById(examDto.getClassId())
                .orElseThrow(() -> new RuntimeException("ClassExam not found"));
        
        Subject subject = subjectRepository.findById(examDto.getClassId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        // Check if exam already exists for this classExam and subject
        examRepository.findByClassExamIdAndSubjectIdAndSchoolId(
                examDto.getClassId(), examDto.getClassId(), schoolId)
                .ifPresent(e -> {
                    throw new RuntimeException("Exam already exists for this class exam and subject");
                });
        
        Exam exam = Exam.builder()
                .name(examDto.getName())
                .classExam(classExam)
                .classEntity(classExam.getClassEntity())
                .subject(subject)
                .school(classExam.getSchool())
                .examDate(examDto.getExamDate() != null ? 
                    new java.sql.Date(examDto.getExamDate().getTime()).toLocalDate() : null)
                .maximumMarks(examDto.getMaximumMarks())
                .passMarks(examDto.getPassingMarks())
                .description("")
                .build();
        
        exam = examRepository.save(exam);
        return convertToDTO(exam);
    }

    @Override
    public ExamDto updateExam(Long id, ExamDto examDto, Long schoolId)
    {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        if (!exam.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to exam");
        }
        
        exam.setName(examDto.getName());
        exam.setExamDate(examDto.getExamDate() != null ? 
            new java.sql.Date(examDto.getExamDate().getTime()).toLocalDate() : null);
        exam.setMaximumMarks(examDto.getMaximumMarks());
        exam.setPassMarks(examDto.getPassingMarks());
        exam.setDescription("");
        
        exam = examRepository.save(exam);
        return convertToDTO(exam);
    }

    @Override
    public void deleteExam(Long id, Long schoolId)
    {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        if (!exam.getSchool().getId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to exam");
        }
        
        examRepository.delete(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getAllExams(Long schoolId)
    {
        List<Exam> exams = examRepository.findBySchoolId(schoolId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int syncExamsForClass(Long classId, Long schoolId)
    {
        int createdCount = 0;
        
        // Get all ClassExams for this class
        List<ClassExam> classExams = classExamRepository.findByClassIdAndSchoolId(classId, schoolId);
        
        // Get all subjects for this class
        List<Subject> subjects = subjectRepository.findByClassEntity_Id(classId);
        
        // For each ClassExam and Subject combination, create Exam if not exists
        for (ClassExam classExam : classExams) {
            for (Subject subject : subjects) {
                // Check if exam already exists
                boolean exists = examRepository.existsByClassExamAndSubjectAndSchoolId(
                        classExam, subject, schoolId);
                
                if (!exists) {
                    // Create new Exam
                    ClassEntity classEntity = classEntityRepository.findById(classId)
                            .orElseThrow(() -> new RuntimeException("Class not found"));
                    
                    Exam exam = Exam.builder()
                            .name(classExam.getExamType().getName() + " - " + subject.getSubjectName())
                            .classExam(classExam)
                            .classEntity(classEntity)
                            .subject(subject)
                            .school(classExam.getSchool())
                            .examDate(classExam.getExamDate())
                            .maximumMarks(classExam.getMaxMarks() != null ? classExam.getMaxMarks().doubleValue() : 100.0)
                            .passMarks(classExam.getPassingMarks() != null ? classExam.getPassingMarks().doubleValue() : 40.0)
                            .description("Auto-synced")
                            .build();
                    
                    examRepository.save(exam);
                    createdCount++;
                }
            }
        }
        
        return createdCount;
    }

    private ExamDto convertToDTO(Exam exam)
    {
        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setExamDate(exam.getExamDate() != null ? java.sql.Date.valueOf(exam.getExamDate()) : null);
        dto.setClassId(exam.getClassEntity() != null ? exam.getClassEntity().getId() : null);
        dto.setClassName(exam.getClassEntity() != null ? exam.getClassEntity().getClassName() : null);
        dto.setMaximumMarks(exam.getMaximumMarks());
        dto.setPassingMarks(exam.getPassMarks());
        
        // Add classExam information
        if (exam.getClassExam() != null) {
            dto.setClassExamId(exam.getClassExam().getId());
            if (exam.getClassExam().getExamType() != null) {
                dto.setExamType(exam.getClassExam().getExamType().getName());
            }
        }
        
        // Add subject information
        if (exam.getSubject() != null) {
            dto.setSubjectId(exam.getSubject().getId());
            dto.setSubjectName(exam.getSubject().getSubjectName());
        }
        
        dto.setDescription(exam.getDescription());
        
        return dto;
    }
}
