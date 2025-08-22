package com.java.slms.serviceImpl;

import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;
import com.java.slms.dto.StudentResponseDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Session;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.FeeStructureRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.ClassEntityService;
import com.java.slms.service.StudentService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.EntityNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassEntityServiceImpl implements ClassEntityService
{
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentService studentService;

    @Override
    @Transactional
    public ClassResponseDto addClass(ClassRequestDto classRequestDto)
    {
        Optional<ClassEntity> existingClass = classEntityRepository
                .findByClassNameIgnoreCaseAndSessionId(classRequestDto.getClassName(), classRequestDto.getSessionId());

        if (existingClass.isPresent())
        {
            log.warn("Class already exists with name '{}' for session ID {}", classRequestDto.getClassName(), classRequestDto.getSessionId());
            throw new AlreadyExistException("Class already exists with this name for the selected active session.");
        }

        Session session = EntityFetcher.fetchByIdOrThrow(
                sessionRepository,
                classRequestDto.getSessionId(),
                EntityNames.SESSION
        );

        if (!session.isActive())
        {
            log.warn("Cannot add class to inactive session with ID {}", classRequestDto.getSessionId());
            throw new WrongArgumentException("Cannot add class to an inactive session");
        }

        ClassEntity classEntity = modelMapper.map(classRequestDto, ClassEntity.class);
        classEntity.setSession(session);
        classEntity.setId(null);
        ClassEntity savedEntity = classEntityRepository.save(classEntity);

        FeeStructure feeStructure = FeeStructure.builder()
                .feesAmount(classRequestDto.getFeesAmount())
                .session(session).classEntity(classEntity)
                .build();

        feeStructureRepository.save(feeStructure);
        ClassResponseDto savedClassDto = modelMapper.map(savedEntity, ClassResponseDto.class);
        if (savedEntity.getStudents() != null && !savedEntity.getStudents().isEmpty())
            savedClassDto.setTotalStudents(savedEntity.getStudents().size());

        var response = modelMapper.map(savedEntity, ClassResponseDto.class);
        response.setFeesAmount(classRequestDto.getFeesAmount());
        return response;
    }

    @Override
    public List<ClassResponseDto> getAllClass()
    {
        log.info("Fetching all classes with session and fee details");

        List<ClassEntity> classEntities = classEntityRepository.findAll();

        return classEntities.stream()
                .map(classEntity ->
                {
                    Long classId = classEntity.getId();
                    Long sessionId = classEntity.getSession() != null ? classEntity.getSession().getId() : null;

                    FeeStructure feeStructure = null;
                    if (sessionId != null)
                    {
                        feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_Id(classId, sessionId)
                                .orElse(null);
                    }

                    ClassResponseDto dto = modelMapper.map(classEntity, ClassResponseDto.class);
                    if (classEntity.getSession() != null)
                    {
                        dto.setSessionId(classEntity.getSession().getId());
                        dto.setSessionName(classEntity.getSession().getName());
                    }
                    dto.setFeesAmount(feeStructure != null ? feeStructure.getFeesAmount() : null);
                    dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
                    List<StudentResponseDto> students = studentService.getStudentsByClassId(classId);
                    dto.setStudents(students);
                    return dto;
                })
                .toList();
    }

    @Override
    public ClassResponseDto getClassByClassIdAndSessionId(Long classId, Long sessionId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSessionId(classId, sessionId)
                .orElseThrow(() ->
                {
                    log.error("Class not found with ClassId: {} and SessionId: {}", classId, sessionId);
                    return new ResourceNotFoundException("Class not found with ClassId: " + classId + " and SessionId: " + sessionId);
                });

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_Id(classId, sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));


        ClassResponseDto dto = modelMapper.map(classEntity, ClassResponseDto.class);
        dto.setSessionId(classEntity.getSession().getId());
        dto.setSessionName(classEntity.getSession().getName());
        dto.setFeesAmount(feeStructure.getFeesAmount());
        dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
        List<StudentResponseDto> students = studentService.getStudentsByClassId(classId);
        dto.setStudents(students);
        return dto;
    }

    public void deleteClassByIdAndSessionId(Long classId, Long sessionId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSessionId(classId, sessionId)
                .orElseThrow(() ->
                {
                    log.error("Class not found with ClassId: {} and SessionId: {}", classId, sessionId);
                    return new ResourceNotFoundException("Class not found with ClassId: " + classId + " and SessionId: " + sessionId);
                });

        if (!classEntity.getSession().isActive())
        {
            log.error("Cannot delete class from inactive session with ID {}", classEntity.getSession().getId());
            throw new WrongArgumentException("Cannot add class to an inactive session");
        }

        classEntityRepository.delete(classEntity);
    }

    @Override
    public ClassResponseDto updateClassNameById(Long id, ClassRequestDto classRequestDto)
    {
        ClassEntity existingClass = classEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));

        // Fetch the session
        Session session = EntityFetcher.fetchByIdOrThrow(
                sessionRepository,
                classRequestDto.getSessionId(),
                EntityNames.SESSION
        );

        // Check if another class with same name and session already exists
        Optional<ClassEntity> duplicateClass = classEntityRepository
                .findByClassNameIgnoreCaseAndSessionId(classRequestDto.getClassName(), classRequestDto.getSessionId());

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(id))
        {
            throw new AlreadyExistException("Class already exists with name: " + classRequestDto.getClassName() +
                    " for the selected session.");
        }

        if (!session.isActive())
        {
            log.warn("Cannot update class due to inactive session with ID {}", classRequestDto.getSessionId());
            throw new WrongArgumentException("Cannot update class due to inactive session");
        }

        // Update the class
        existingClass.setClassName(classRequestDto.getClassName());
        existingClass.setSession(session);

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_Id(id, classRequestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

        feeStructure.setFeesAmount(classRequestDto.getFeesAmount());
        feeStructure.setSession(session);
        feeStructure.setClassEntity(existingClass);

        ClassEntity updatedClass = classEntityRepository.save(existingClass);

        ClassResponseDto dto = modelMapper.map(updatedClass, ClassResponseDto.class);
        dto.setTotalStudents(updatedClass.getStudents() != null ? updatedClass.getStudents().size() : 0);
        return dto;
    }


}
