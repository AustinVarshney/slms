package com.java.slms.serviceImpl;

import com.java.slms.dto.SpecificSubject;
import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Subject;
import com.java.slms.model.Teacher;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.SubjectService;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService
{

    private final SubjectRepository subjectRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;
    private final TeacherRepository teacherRepository;

    @Override
    public SubjectDto addSubject(SubjectDto subjectDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        ensureActiveSession(classEntity);

        if (subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(
                subjectDto.getSubjectName(), subjectDto.getClassId()))
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() +
                    "' already exists in class with ID '" + subjectDto.getClassId() + "'.");
        }

        validateTeacherStatus(subjectDto.getTeacherId());

        Teacher teacher = teacherRepository.findById(subjectDto.getTeacherId()).get(); // already validated

        Subject subject = new Subject();
        subject.setSubjectName(subjectDto.getSubjectName());
        subject.setClassEntity(classEntity);
        subject.setTeacher(teacher);

        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
    }

    @Override
    public List<SubjectDto> getAllSubjects()
    {
        List<Subject> subjects = subjectRepository.findAll();
        return subjects.stream().map(s ->
        {
            SubjectDto dto = modelMapper.map(s, SubjectDto.class);
            dto.setClassName(s.getClassEntity().getClassName());
            dto.setSessionId(s.getClassEntity().getSession().getId());
            return dto;
        }).toList();
    }

    @Override
    public SubjectDto getSubjectById(Long id)
    {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + id + "' not found."));

        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
        dto.setClassId(subject.getClassEntity().getId());
        dto.setSessionId(subject.getClassEntity().getSession().getId());
        return dto;
    }

    @Override
    public void deleteSubject(Long subjectId, Long classId)
    {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID '" + classId + "' not found."));

        if (!classEntity.getSession().isActive())
        {
            log.error("Cannot delete Subject from class '{}' because the session with ID {} is inactive",
                    classEntity.getClassName(), classEntity.getSession().getId());
            throw new WrongArgumentException("Cannot delete subject from a class in an inactive session");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found."));

        if (!subject.getClassEntity().getId().equals(classId))
        {
            throw new ResourceNotFoundException("Subject with ID '" + subjectId + "' does not belong to class with ID '" + classId + "'.");
        }

        subjectRepository.delete(subject);
    }

    @Override
    public SubjectDto updateSubjectInfoById(Long subjectId, SubjectDto subjectDto)
    {
        Subject existing = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));

        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        ensureActiveSession(classEntity);
        validateTeacherStatus(subjectDto.getTeacherId());

        boolean subjectExists = subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(
                subjectDto.getSubjectName(), subjectDto.getClassId());

        if (!existing.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName()) && subjectExists)
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() +
                    "' already exists in class with ID: " + subjectDto.getClassId());
        }

        Teacher teacher = teacherRepository.findById(subjectDto.getTeacherId()).get(); // already validated

        existing.setSubjectName(subjectDto.getSubjectName());
        existing.setClassEntity(classEntity);
        existing.setTeacher(teacher);

        Subject updated = subjectRepository.save(existing);
        return mapToDto(updated);
    }

    @Override
    public List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(bulkDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + bulkDto.getClassId()));

        if (!classEntity.getSession().isActive())
        {
            log.error("Cannot add Subject to class '{}' because the session with ID {} is inactive",
                    classEntity.getClassName(), classEntity.getSession().getId());
            throw new WrongArgumentException("Cannot add subject to a class in an inactive session");
        }

        List<Subject> newSubjects = new ArrayList<>();
        List<String> skippedSubjects = new ArrayList<>();

        for (SpecificSubject subject1 : bulkDto.getSubjects())
        {
            if (subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subject1.getSubjectName().trim(), bulkDto.getClassId()))
            {
                skippedSubjects.add(subject1.getSubjectName());
                continue;
            }

            Subject subject = new Subject();
            subject.setSubjectName(subject1.getSubjectName().trim());
            subject.setClassEntity(classEntity);

            Teacher teacher = teacherRepository.findById(subject1.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subject1.getTeacherId()));

            subject.setTeacher(teacher);
            newSubjects.add(subject);
        }

        List<Subject> savedSubjects = subjectRepository.saveAll(newSubjects);

        if (!skippedSubjects.isEmpty())
        {
            log.warn("Skipped duplicate subjects: {}", skippedSubjects);
        }

        return savedSubjects.stream()
                .map(subject ->
                {
                    SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
                    dto.setClassName(classEntity.getClassName());
                    dto.setSessionId(classEntity.getSession().getId());
                    return dto;
                }).toList();
    }

    public List<SubjectDto> getSubjectsByClassId(Long classId)
    {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + classId));

        List<Subject> subjects = subjectRepository.findByClassEntity_Id(classId);

        if (subjects.isEmpty())
        {
            throw new ResourceNotFoundException("No subjects found for class ID: " + classId);
        }

        return subjects.stream()
                .map(subject ->
                {
                    SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
                    if (subject.getClassEntity() != null && subject.getClassEntity().getSession() != null)
                    {
                        dto.setSessionId(subject.getClassEntity().getSession().getId());
                    }
                    return dto;
                })
                .toList();

    }

    private void ensureActiveSession(ClassEntity classEntity)
    {
        if (!classEntity.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot perform operation. Session for class '" +
                    classEntity.getClassName() + "' is inactive.");
        }
    }

    private SubjectDto mapToDto(Subject subject)
    {
        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
        dto.setClassId(subject.getClassEntity().getId());
        dto.setClassName(subject.getClassEntity().getClassName());
        dto.setTeacherId(subject.getTeacher().getId());
        dto.setSessionId(subject.getClassEntity().getSession().getId());
        return dto;
    }

    private void validateTeacherStatus(Long teacherId)
    {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (teacher.getStatus() == UserStatus.INACTIVE)
        {
            throw new AlreadyExistException("Teacher is already inactive");
        }
    }

}
