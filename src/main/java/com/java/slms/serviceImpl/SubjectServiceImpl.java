package com.java.slms.serviceImpl;

import com.java.slms.dto.SpecificSubject;
import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.School;
import com.java.slms.model.Subject;
import com.java.slms.model.Teacher;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.SubjectService;
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
    private final SchoolRepository schoolRepository;

    @Override
    public SubjectDto addSubject(SubjectDto subjectDto, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));

        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(subjectDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        if (subjectRepository.existsBySubjectNameAndClassIdAndSchoolId(
                subjectDto.getSubjectName(), subjectDto.getClassId(), schoolId))
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() +
                    "' already exists in class with ID '" + subjectDto.getClassId() + "'.");
        }

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(subjectDto.getTeacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subjectDto.getTeacherId()));

        String displayName = subjectDto.getSubjectName() + " - " + classEntity.getClassName();

        Subject subject = new Subject();
        subject.setSubjectName(displayName);
        subject.setClassEntity(classEntity);
        subject.setTeacher(teacher);
        subject.setSchool(school);

        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
    }

    @Override
    public List<SubjectDto> getAllSubjects(Long schoolId)
    {
        List<Subject> subjects = subjectRepository.findAllBySchoolId(schoolId);
        return subjects.stream().map(s ->
        {
            SubjectDto dto = modelMapper.map(s, SubjectDto.class);
            dto.setClassName(s.getClassEntity().getClassName());
            dto.setSessionId(s.getClassEntity().getSession().getId());
            dto.setSchoolId(schoolId);
            return dto;
        }).toList();
    }

    @Override
    public SubjectDto getSubjectById(Long id, Long schoolId)
    {
        Subject subject = subjectRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject with ID " + id + " not found in school with ID " + schoolId));

        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
        dto.setClassId(subject.getClassEntity().getId());
        dto.setSessionId(subject.getClassEntity().getSession().getId());
        dto.setSchoolId(subject.getSchool().getId());
        return dto;
    }

    @Override
    public void deleteSubject(Long subjectId, Long schoolId)
    {
        Subject subject = subjectRepository.findByIdAndSchoolId(subjectId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject with ID '" + subjectId + "' not found in school with ID '" + schoolId + "'."));

        ClassEntity classEntity = subject.getClassEntity();

        if (classEntity == null)
        {
            throw new ResourceNotFoundException(
                    "Subject with ID '" + subjectId + "' is not associated with any class.");
        }

        if (!classEntity.getSchool().getId().equals(schoolId))
        {
            throw new ResourceNotFoundException(
                    "Class associated with subject does not belong to school with ID '" + schoolId + "'.");
        }

        if (!classEntity.getSession().isActive())
        {
            log.error("Cannot delete Subject '{}' because the session with ID '{}' is inactive",
                    subject.getSubjectName(), classEntity.getSession().getId());
            throw new WrongArgumentException("Cannot delete subject from a class in an inactive session");
        }

        subjectRepository.delete(subject);
    }

    @Override
    public SubjectDto updateSubjectInfoById(Long subjectId, SubjectDto subjectDto, Long schoolId)
    {
        Subject existing = subjectRepository.findSubjectByIdAndSchoolId(subjectId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));

        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(subjectDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(subjectDto.getTeacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subjectDto.getTeacherId()));

        boolean subjectExists = subjectRepository.existsBySubjectNameAndClassIdAndSchoolId(
                subjectDto.getSubjectName(), subjectDto.getClassId(), schoolId);

        if (!existing.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName()) && subjectExists)
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() +
                    "' already exists in class with ID: " + subjectDto.getClassId());
        }
        String displayName = subjectDto.getSubjectName() + " - " + classEntity.getClassName();

        existing.setSubjectName(displayName);
        existing.setClassEntity(classEntity);
        
        // Teacher is optional - can be updated or removed
        if (subjectDto.getTeacherId() != null)
        {
            Teacher teacherToAssign = teacherRepository.findById(subjectDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + subjectDto.getTeacherId()));
            existing.setTeacher(teacherToAssign);
        }
        else
        {
            existing.setTeacher(null);
        }

        Subject updated = subjectRepository.save(existing);
        return mapToDto(updated);
    }

    @Override
    public List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto, Long schoolId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(bulkDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + bulkDto.getClassId()));

        List<Subject> newSubjects = new ArrayList<>();
        List<String> skippedSubjects = new ArrayList<>();
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));


        for (SpecificSubject subject1 : bulkDto.getSubjects())
        {
            if (subjectRepository.existsBySubjectNameAndClassIdAndSchoolId(subject1.getSubjectName().trim(), bulkDto.getClassId(), schoolId))
            {
                skippedSubjects.add(subject1.getSubjectName());
                continue;
            }

            Subject subject = new Subject();

            String displayName = subject1.getSubjectName() + " - " + classEntity.getClassName();

            subject.setSubjectName(displayName);
            subject.setClassEntity(classEntity);
            subject.setSchool(school);

            Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(subject1.getTeacherId(), schoolId)
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
                    dto.setSchoolId(classEntity.getSchool().getId());
                    return dto;
                }).toList();
    }

    @Override
    public List<SubjectDto> getSubjectsByClassId(Long classId, Long schoolId)
    {
        List<Subject> subjects = subjectRepository.findByClassEntityIdAndSchoolId(classId, schoolId);

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
                        dto.setSchoolId(subject.getSchool().getId());
                    }
                    return dto;
                })
                .toList();

    }

    private SubjectDto mapToDto(Subject subject)
    {
        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
        dto.setClassId(subject.getClassEntity().getId());
        dto.setClassName(subject.getClassEntity().getClassName());
        
        // Teacher is optional - handle null safely
        if (subject.getTeacher() != null)
        {
            dto.setTeacherId(subject.getTeacher().getId());
            dto.setTeacherName(subject.getTeacher().getName());
        }
        
        dto.setSessionId(subject.getClassEntity().getSession().getId());
        dto.setSchoolId(subject.getSchool().getId());
        return dto;
    }

}
