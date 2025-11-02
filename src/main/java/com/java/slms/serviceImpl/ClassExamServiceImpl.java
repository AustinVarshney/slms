package com.java.slms.serviceImpl;

import com.java.slms.dto.ClassExamBulkRequestDto;
import com.java.slms.dto.ClassExamEntry;
import com.java.slms.dto.ClassExamResponseDto;
import com.java.slms.dto.ClassExamUpdateDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.ClassExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassExamServiceImpl implements ClassExamService
{

    private final ClassExamRepository classExamRepository;
    private final ExamTypeRepository examTypeRepository;
    private final ClassEntityRepository classRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;
    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final ScoreRepository scoreRepository;

    @Override
    public void assignExamTypeToMultipleClasses(ClassExamBulkRequestDto dto, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        ExamType examType = examTypeRepository.findByIdAndSchoolId(dto.getExamTypeId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found for this school"));

        for (ClassExamEntry entry : dto.getClassExams())
        {

            ClassEntity classEntity = classRepository.findByIdAndSchoolIdAndSessionActive(entry.getClassId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + entry.getClassId()));

            // Check if already assigned - if so, update it instead of creating duplicate
            // Note: This query filters by active session to avoid conflicts with old sessions
            Optional<ClassExam> existingClassExam = classExamRepository.findByClassEntityAndExamTypeAndSchoolId(classEntity, examType, schoolId);
            
            ClassExam classExam;
            if (existingClassExam.isPresent())
            {
                // Update existing assignment
                classExam = existingClassExam.get();
                classExam.setExamDate(entry.getExamDate());
                classExam.setMaxMarks(entry.getMaxMarks());
                classExam.setPassingMarks(entry.getPassingMarks());
                log.info("Updating existing ExamType assignment - ClassExam ID: {}, Class: {}, ExamType: {}", 
                         classExam.getId(), classEntity.getClassName(), examType.getName());
            }
            else
            {
                // Create new assignment
                classExam = ClassExam.builder()
                        .classEntity(classEntity)
                        .examType(examType)
                        .school(school)
                        .examDate(entry.getExamDate())
                        .maxMarks(entry.getMaxMarks())
                        .passingMarks(entry.getPassingMarks())
                        .build();
                log.info("Creating new ExamType assignment for Class: {}, ExamType: {}", 
                         classEntity.getClassName(), examType.getName());
            }

            try {
                ClassExam savedClassExam = classExamRepository.save(classExam);
                log.info("Successfully saved ClassExam ID: {}", savedClassExam.getId());
                
                // Create Exam entries for each subject in this class
                createExamsForClassExam(savedClassExam, schoolId);
            } catch (Exception e) {
                log.error("Error saving ClassExam for class {} and exam type {}: {}", 
                         classEntity.getClassName(), examType.getName(), e.getMessage());
                throw new WrongArgumentException("Duplicate Entry detected for class " + classEntity.getClassName() + 
                                                 " and exam type " + examType.getName() + ". Please check existing assignments.");
            }
        }
    }
    
    /**
     * Create individual Exam entries for each subject in the class
     * This populates the Exam table which is needed for scores
     */
    private void createExamsForClassExam(ClassExam classExam, Long schoolId) {
        try {
            // Get all subjects for this class
            List<Subject> subjects = subjectRepository.findByClassEntity_Id(classExam.getClassEntity().getId());
            
            for (Subject subject : subjects) {
                // Check if exam already exists for this combination
                boolean exists = examRepository.existsByClassExamAndSubjectAndSchoolId(classExam, subject, schoolId);
                if (exists) {
                    log.debug("Exam already exists for subject: {} in classExam: {}", subject.getSubjectName(), classExam.getId());
                    continue;
                }
                
                // Create Exam entry
                Exam exam = Exam.builder()
                        .name(classExam.getExamType().getName() + " - " + subject.getSubjectName())
                        .classExam(classExam)
                        .classEntity(classExam.getClassEntity())
                        .subject(subject)
                        .school(classExam.getSchool())
                        .examDate(classExam.getExamDate())
                        .maximumMarks(classExam.getMaxMarks().doubleValue())
                        .passMarks(classExam.getPassingMarks().doubleValue())
                        .description("Auto-generated from ClassExam")
                        .build();
                
                examRepository.save(exam);
                log.info("Created Exam: {} for class: {}", exam.getName(), classExam.getClassEntity().getClassName());
            }
        } catch (Exception e) {
            log.error("Failed to create exams for ClassExam: {}", classExam.getId(), e);
        }
    }

    @Override
    public List<ClassExamResponseDto> getExamsByClass(Long classId, Long schoolId)
    {
        // Only get exams for classes in active session
        List<ClassExam> exams = classExamRepository.findByClassIdAndSchoolIdWithActiveSession(classId, schoolId);
        return exams.stream()
                .map(exam -> ClassExamResponseDto.builder()
                        .id(exam.getId())
                        .classId(exam.getClassEntity().getId())
                        .className(exam.getClassEntity().getClassName())
                        .examTypeId(exam.getExamType().getId())
                        .examTypeName(exam.getExamType().getName())
                        .examDate(exam.getExamDate())
                        .maxMarks(exam.getMaxMarks())
                        .passingMarks(exam.getPassingMarks())
                        .build())
                .toList();
    }

    @Override
    public List<ClassExamResponseDto> getClassesByExamType(Long examTypeId, Long schoolId)
    {
        ExamType examType = examTypeRepository.findByIdAndSchoolId(examTypeId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamType not found with ID: " + examTypeId));

        List<ClassExam> classExams = classExamRepository.findByExamTypeAndSchoolId(examType, schoolId);
        
        return classExams.stream()
                .map(exam -> ClassExamResponseDto.builder()
                        .id(exam.getId())
                        .classId(exam.getClassEntity().getId())
                        .className(exam.getClassEntity().getClassName())
                        .examTypeId(exam.getExamType().getId())
                        .examTypeName(exam.getExamType().getName())
                        .examDate(exam.getExamDate())
                        .maxMarks(exam.getMaxMarks())
                        .passingMarks(exam.getPassingMarks())
                        .build())
                .toList();
    }

    @Override
    public void updateClassExam(Long classId, Long examTypeId, ClassExamUpdateDto dto, Long schoolId)
    {
        ClassExam classExam = classExamRepository.findByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(classId, examTypeId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ClassExam not found for classId: " + classId + ", examTypeId: " + examTypeId +
                                " in schoolId: " + schoolId + " with an active session."));

        // Update all fields
        classExam.setExamDate(dto.getExamDate());
        classExam.setMaxMarks(dto.getMaxMarks());
        classExam.setPassingMarks(dto.getPassingMarks());

        classExamRepository.save(classExam);
        log.info("Updated ClassExam for classId={}, examTypeId={} with maxMarks={}, passingMarks={}", 
                classId, examTypeId, dto.getMaxMarks(), dto.getPassingMarks());
    }

    @Override
    @Transactional
    public void deleteClassExam(Long classId, Long examTypeId, Long schoolId)
    {
        log.info("Deleting ClassExam for classId={}, examTypeId={}, schoolId={}", classId, examTypeId, schoolId);
        
        // First, find the ClassExam to get its ID
        Optional<ClassExam> classExamOpt = classExamRepository.findByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(
                classId, examTypeId, schoolId);
        
        if (classExamOpt.isEmpty()) {
            throw new ResourceNotFoundException("No active ClassExam found for deletion with classId: " + classId +
                    ", examTypeId: " + examTypeId + ", schoolId: " + schoolId);
        }
        
        ClassExam classExam = classExamOpt.get();
        Long classExamId = classExam.getId();
        
        // Step 1: Get all Exams for this ClassExam
        List<Exam> exams = examRepository.findByClassExamIdAndSchoolId(classExamId, schoolId);
        
        if (!exams.isEmpty()) {
            log.info("Found {} Exam entries to delete for ClassExam id: {}", exams.size(), classExamId);
            
            // Step 2: For each Exam, delete associated Scores
            for (Exam exam : exams) {
                List<Score> scores = scoreRepository.findByExam_IdAndStudent_CurrentClass_Id(
                    exam.getId(), 
                    classExam.getClassEntity().getId()
                );
                if (!scores.isEmpty()) {
                    scoreRepository.deleteAll(scores);
                    log.info("Deleted {} Score entries for Exam id: {}", scores.size(), exam.getId());
                }
            }
            
            // Step 3: Delete all Exams
            examRepository.deleteAll(exams);
            log.info("Deleted {} Exam entries for ClassExam id: {}", exams.size(), classExamId);
        }
        
        // Step 4: Finally, delete the ClassExam itself using repository delete (not custom query)
        classExamRepository.delete(classExam);
        log.info("Successfully deleted ClassExam with cascade for classId={}, examTypeId={}, schoolId={}", classId, examTypeId, schoolId);
    }


}
