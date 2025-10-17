package com.java.slms.serviceImpl;

import com.java.slms.dto.ClassExamBulkRequestDto;
import com.java.slms.dto.ClassExamEntry;
import com.java.slms.dto.ClassExamResponseDto;
import com.java.slms.dto.ClassExamUpdateDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.ClassExam;
import com.java.slms.model.ExamType;
import com.java.slms.model.School;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.ClassExamRepository;
import com.java.slms.repository.ExamTypeRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.ClassExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            // Check if already assigned to avoid duplicates
            boolean exists = classExamRepository.existsByClassAndExamTypeAndSchoolId(classEntity, examType, schoolId);
            if (exists)
            {
                log.warn("ExamType already assigned to class: {}", classEntity.getClassName());
                continue;
            }

            ClassExam classExam = ClassExam.builder()
                    .classEntity(classEntity)
                    .examType(examType)
                    .school(school)
                    .examDate(entry.getExamDate())
                    .build();

            classExamRepository.save(classExam);
            log.info("Assigned {} to class {}", examType.getName(), classEntity.getClassName());
        }
    }

    @Override
    public List<ClassExamResponseDto> getExamsByClass(Long classId, Long schoolId)
    {
        List<ClassExam> exams = classExamRepository.findByClassIdAndSchoolId(classId, schoolId);
        return exams.stream()
                .map(exam -> ClassExamResponseDto.builder()
                        .id(exam.getId())
                        .classId(exam.getClassEntity().getId())
                        .className(exam.getClassEntity().getClassName())
                        .examTypeId(exam.getExamType().getId())
                        .examTypeName(exam.getExamType().getName())
                        .examDate(exam.getExamDate())
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


        classExam.setExamDate(dto.getExamDate());

        classExamRepository.save(classExam);
        log.info("Updated ClassExam for classId={}, examTypeId={}", classId, examTypeId);
    }

    @Override
    @Transactional
    public void deleteClassExam(Long classId, Long examTypeId, Long schoolId)
    {
        int deletedCount = classExamRepository.deleteByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(classId, examTypeId, schoolId);

        if (deletedCount == 0)
        {
            throw new ResourceNotFoundException("No active ClassExam found for deletion with classId: " + classId +
                    ", examTypeId: " + examTypeId + ", schoolId: " + schoolId);
        }

        log.info("Deleted ClassExam for classId={}, examTypeId={}, schoolId={}", classId, examTypeId, schoolId);
    }


}
