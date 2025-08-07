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

    public SubjectDto addSubject(SubjectDto subjectDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        if (subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subjectDto.getSubjectName(), subjectDto.getClassId()))
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() + "' already exists in class with ID '" + subjectDto.getClassId() + "'.");
        }

        // Find Teacher by teacherId
        Teacher teacher = null;
        if (subjectDto.getTeacherId() != null)
        {
            teacher = teacherRepository.findById(subjectDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subjectDto.getTeacherId()));
        }
        else
        {
            throw new WrongArgumentException("Teacher ID must be provided for a subject.");
        }

        Subject subject = new Subject();
        subject.setSubjectName(subjectDto.getSubjectName());
        subject.setClassEntity(classEntity);
        subject.setTeacher(teacher);

        Subject saved = subjectRepository.save(subject);

        SubjectDto savedDto = modelMapper.map(saved, SubjectDto.class);
        savedDto.setClassId(classEntity.getId());
        savedDto.setTeacherId(teacher.getId());

        return savedDto;
    }

    @Override
    public List<SubjectDto> getAllSubjects()
    {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectDto> dtos = new ArrayList<>();
        for (Subject s : subjects)
        {
            SubjectDto dto = modelMapper.map(s, SubjectDto.class);
            dto.setClassName(s.getClassEntity().getClassName());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public SubjectDto getSubjectById(Long id)
    {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + id + "' not found."));

        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);

        dto.setClassId(subject.getClassEntity().getId());

        return dto;
    }

    @Override
    public void deleteSubject(Long subjectId, Long classId)
    {
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class with ID '" + classId + "' not found.");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found."));

        // Check if the subject belongs to the given class
        if (!subject.getClassEntity().getId().equals(classId))
        {
            throw new ResourceNotFoundException("Subject with ID '" + subjectId + "' does not belong to class with ID '" + classId + "'.");
        }

        subjectRepository.delete(subject);
    }

    public SubjectDto updateSubjectById(Long subjectId, SubjectDto subjectDto)
    {
        Subject existing = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));

        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        // Check for duplicate subject name in the same class (except for current subject)
        boolean subjectExists = subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subjectDto.getSubjectName(), subjectDto.getClassId());
        if (!existing.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName()) && subjectExists)
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() + "' already exists in class with ID: " + subjectDto.getClassId());
        }

        Teacher teacher = null;
        if (subjectDto.getTeacherId() != null)
        {
            teacher = teacherRepository.findById(subjectDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subjectDto.getTeacherId()));
        }
        else
        {
            throw new WrongArgumentException("Teacher ID must be provided for a subject.");
        }

        existing.setSubjectName(subjectDto.getSubjectName());
        existing.setClassEntity(classEntity);
        existing.setTeacher(teacher); // Update teacher

        Subject updated = subjectRepository.save(existing);

        SubjectDto updatedDto = modelMapper.map(updated, SubjectDto.class);
        updatedDto.setClassName(classEntity.getClassName());
        updatedDto.setTeacherId(teacher.getId());

        return updatedDto;
    }

    @Override
    public List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(bulkDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + bulkDto.getClassId()));

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

//             optional: assign a teacher if your DTO supports teacherId
            Teacher teacher = teacherRepository.findById(subject1.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + subject1.getTeacherId()));

            subject.setTeacher(teacher);
            newSubjects.add(subject);
        }

        List<Subject> savedSubjects = subjectRepository.saveAll(newSubjects);

        if (!skippedSubjects.isEmpty())
        {
            // Log or handle skipped subjects
            log.warn("Skipped duplicate subjects: {}", skippedSubjects);
        }

        return savedSubjects.stream()
                .map(subject ->
                {
                    SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
                    dto.setClassName(classEntity.getClassName());
                    return dto;
                }).toList();
    }

    public List<SubjectDto> getSubjectsByClassId(Long classId)
    {
        List<Subject> subjects = subjectRepository.findByClassEntity_Id(classId);

        if (subjects.isEmpty())
        {
            throw new ResourceNotFoundException("No subjects found for class ID: " + classId);
        }

        // Convert the list of Subject entities to SubjectDto
        return subjects.stream()
                .map(subject -> modelMapper.map(subject, SubjectDto.class))
                .toList();
    }
}