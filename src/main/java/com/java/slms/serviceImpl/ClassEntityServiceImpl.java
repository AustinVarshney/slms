package com.java.slms.serviceImpl;


import com.java.slms.dto.*;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.ClassEntityService;
import com.java.slms.service.FeeService;
import com.java.slms.service.StudentService;
import com.java.slms.util.ClassNameParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class ClassEntityServiceImpl implements ClassEntityService
{
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentService studentService;
    private final FeeService feeService;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public ClassResponseDto addClass(Long schoolId, ClassRequestDto classRequestDto)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new WrongArgumentException("Session not found or does not belong to the provided school, or is not active."));

        // Parse class name to handle various formats like "L.K.G.-A", "LKG-A", "11-1A"
        ClassNameParser.ParsedClassName parsed = ClassNameParser.parse(classRequestDto.getClassName().trim());
        String className = parsed.getFullName(); // Store as standardized format: "L.K.G.-A", "LKG-A", etc.

        log.info("Parsed class name: '{}' -> '{}'", classRequestDto.getClassName(), className);

        // Check for duplicate class with simple name in this session
        Optional<ClassEntity> existingClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(className, session.getId(), schoolId);

        if (existingClass.isPresent())
        {
            log.warn("Class already exists with name '{}' for session ID {}", className, session.getId());
            throw new AlreadyExistException("Class already exists with this name for the selected active session.");
        }

        // Set class name into DTO
        classRequestDto.setClassName(className);

        // Handle teacher assignment
        Teacher teacher = null;
        if (classRequestDto.getClassTeacherId() != null)
        {
            teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(classRequestDto.getClassTeacherId(), schoolId).orElseThrow(() -> new ResourceNotFoundException("Teacher with ID " + classRequestDto.getClassTeacherId() + " not found for school ID " + schoolId));

            if (isTeacherAssignedToAnotherClass(schoolId, classRequestDto.getClassTeacherId()))
            {
                throw new WrongArgumentException("Teacher with id " + classRequestDto.getClassTeacherId() + " is already assigned to another class.");
            }
        }

        // Map and save class
        ClassEntity classEntity = modelMapper.map(classRequestDto, ClassEntity.class);
        classEntity.setSession(session);
        classEntity.setClassTeacher(teacher);
        classEntity.setId(null);
        classEntity.setSchool(school);
        ClassEntity savedEntity = classEntityRepository.save(classEntity);

        // Save fee structure
        FeeStructure feeStructure = FeeStructure.builder().feesAmount(classRequestDto.getFeesAmount()).session(session).classEntity(savedEntity).school(school).build();

        feeStructureRepository.save(feeStructure);

        // Build response
        ClassResponseDto response = modelMapper.map(savedEntity, ClassResponseDto.class);
        response.setFeesAmount(classRequestDto.getFeesAmount());

        if (savedEntity.getClassTeacher() != null)
        {
            response.setClassTeacherId(savedEntity.getClassTeacher().getId());
            response.setClassTeacherName(savedEntity.getClassTeacher().getName());
        }
        else
        {
            response.setClassTeacherId(null);
            response.setClassTeacherName(null);
        }

        response.setTotalStudents(savedEntity.getStudents() != null ? savedEntity.getStudents().size() : 0);

        return response;
    }

    @Override
    public List<ClassInfoResponse> getAllClassInActiveSession(Long schoolId)
    {
        log.info("Fetching all classes with session and fee details");

        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        List<ClassEntity> classEntities = classEntityRepository.findBySession_IdAndSchool_Id(activeSession.getId(), schoolId);

        return classEntities.stream().map(classEntity ->
        {
            Long classId = classEntity.getId();
            Long sessionId = activeSession.getId();

            FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(classId, sessionId, schoolId).orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class ID " + classId + " and session ID " + sessionId));

            ClassInfoResponse dto = modelMapper.map(classEntity, ClassInfoResponse.class);
            dto.setSessionId(sessionId);
            dto.setSessionName(activeSession.getName());

            if (classEntity.getClassTeacher() != null)
            {
                dto.setClassTeacherId(classEntity.getClassTeacher().getId());
                dto.setClassTeacherName(classEntity.getClassTeacher().getName());
            }

            dto.setFeesAmount(feeStructure.getFeesAmount());
            dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);

            List<StudentResponseDto> students = studentService.getStudentsByClassId(classId, schoolId);
            dto.setStudents(students);
            dto.setFeeCollectionRate(calculateFeeCollectionRate(students, schoolId));

            return dto;
        }).toList();
    }

    @Override
    public List<ClassInfoResponse> getAllClassesBySession(Long schoolId, Long sessionId)
    {
        log.info("Fetching all classes for session: {} in school: {}", sessionId, schoolId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        List<ClassEntity> classEntities = classEntityRepository.findBySession_IdAndSchool_Id(sessionId, schoolId);

        return classEntities.stream().map(classEntity ->
        {
            Long classId = classEntity.getId();

            FeeStructure feeStructure = feeStructureRepository
                    .findByClassEntity_IdAndSession_IdAndSchool_Id(classId, sessionId, schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fee structure not found for class ID " + classId + " and session ID " + sessionId));

            ClassInfoResponse dto = modelMapper.map(classEntity, ClassInfoResponse.class);
            dto.setSessionId(sessionId);
            dto.setSessionName(session.getName());

            if (classEntity.getClassTeacher() != null)
            {
                dto.setClassTeacherId(classEntity.getClassTeacher().getId());
                dto.setClassTeacherName(classEntity.getClassTeacher().getName());
            }

            dto.setFeesAmount(feeStructure.getFeesAmount());
            dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);

            List<StudentResponseDto> students = studentService.getStudentsByClassId(classId, schoolId);
            dto.setStudents(students);
            dto.setFeeCollectionRate(calculateFeeCollectionRate(students, schoolId));

            return dto;
        }).toList();
    }

    @Override
    public ClassInfoResponse getClassByClassIdAndSessionId(Long schoolId, Long classId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolId(classId, schoolId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ClassId: " + classId));

        FeeStructure feeStructure = feeStructureRepository.findByClassIdAndSchoolId(classId, schoolId).orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

        ClassInfoResponse dto = modelMapper.map(classEntity, ClassInfoResponse.class);
        dto.setSessionId(classEntity.getSession().getId());
        dto.setSessionName(classEntity.getSession().getName());
        dto.setFeesAmount(feeStructure.getFeesAmount());

        if (classEntity.getClassTeacher() != null)
        {
            dto.setClassTeacherId(classEntity.getClassTeacher().getId());
            dto.setClassTeacherName(classEntity.getClassTeacher().getName());
        }

        dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);

        List<StudentResponseDto> students = studentService.getStudentsByClassId(classId, schoolId);
        dto.setFeeCollectionRate(calculateFeeCollectionRate(students, schoolId));
        dto.setStudents(students);
        return dto;
    }

    public void deleteClassByIdAndSessionId(Long schoolId, Long classId, Long sessionId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSessionIdAndSchoolId(classId, sessionId, schoolId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ClassId: " + classId + " and SessionId: " + sessionId));

        if (!classEntity.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot delete class from an inactive session");
        }

        classEntityRepository.delete(classEntity);
    }

    @Override
    public ClassInfoResponse updateClassNameById(Long schoolId, Long id, ClassRequestDto classRequestDto)
    {
        ClassEntity existingClass = classEntityRepository.findByIdAndSchoolId(id, schoolId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new WrongArgumentException("Session not found or does not belong to the provided school, or is not active."));

        // Parse class name to handle various formats
        ClassNameParser.ParsedClassName parsed = ClassNameParser.parse(classRequestDto.getClassName().trim());
        String className = parsed.getFullName();
        
        log.info("Updating class name: '{}' -> '{}'", classRequestDto.getClassName(), className);
        
        classRequestDto.setClassName(className);

        Optional<ClassEntity> duplicateClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(className, session.getId(), schoolId);

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(id))
        {
            throw new AlreadyExistException("Class already exists with name: " + className + " for the selected session.");
        }

        Teacher teacher = null;
        if (classRequestDto.getClassTeacherId() != null)
        {
            teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(classRequestDto.getClassTeacherId(), schoolId).orElseThrow(() -> new ResourceNotFoundException("Teacher with ID " + classRequestDto.getClassTeacherId() + " not found for school ID " + schoolId));

            if (isTeacherAssignedToAnotherClass(schoolId, classRequestDto.getClassTeacherId()) && (existingClass.getClassTeacher() == null || !existingClass.getClassTeacher().getId().equals(classRequestDto.getClassTeacherId())))
            {
                throw new WrongArgumentException("Teacher with id " + classRequestDto.getClassTeacherId() + " is already assigned to another class.");
            }
        }

        existingClass.setClassName(className);
        existingClass.setSession(session);
        existingClass.setClassTeacher(teacher);

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(id, session.getId(), schoolId).orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

        feeStructure.setFeesAmount(classRequestDto.getFeesAmount());
        feeStructure.setSession(session);
        feeStructure.setClassEntity(existingClass);
        feeStructureRepository.save(feeStructure);

        ClassEntity updatedClass = classEntityRepository.save(existingClass);

        ClassInfoResponse dto = modelMapper.map(updatedClass, ClassInfoResponse.class);
        dto.setTotalStudents(updatedClass.getStudents() != null ? updatedClass.getStudents().size() : 0);

        if (updatedClass.getClassTeacher() != null)
        {
            dto.setClassTeacherId(updatedClass.getClassTeacher().getId());
            dto.setClassTeacherName(updatedClass.getClassTeacher().getName());
        }
        else
        {
            dto.setClassTeacherId(null);
            dto.setClassTeacherName(null);
        }

        return dto;
    }

    private double calculateFeeCollectionRate(List<StudentResponseDto> students, Long schoolId)
    {
        double totalOfRates = 0.0;
        int countedStudents = 0;

        for (StudentResponseDto studentDto : students)
        {
            try
            {
                FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(studentDto.getPanNumber(), schoolId);
                if (feeCatalog.getTotalAmount() > 0)
                {
                    double rate = feeCatalog.getTotalPaid() / feeCatalog.getTotalAmount();
                    totalOfRates += rate;
                    countedStudents++;
                }
            } catch (ResourceNotFoundException ex)
            {
                log.warn("Fee catalog not found for student PAN: {}", studentDto.getPanNumber());
            } catch (Exception ex)
            {
                log.error("Error fetching fee catalog for student PAN: {}", studentDto.getPanNumber(), ex);
            }
        }

        if (countedStudents == 0)
        {
            return 0.0;
        }

        BigDecimal bd = new BigDecimal((totalOfRates / countedStudents) * 100);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private boolean isTeacherAssignedToAnotherClass(Long schoolId, Long teacherId)
    {
        if (teacherId == null)
        {
            return false;
        }
        List<ClassInfoResponse> activeClasses = getAllClassInActiveSession(schoolId);
        return activeClasses.stream().anyMatch(classInfo -> teacherId.equals(classInfo.getClassTeacherId()));
    }
}
