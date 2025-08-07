package com.java.slms.serviceImpl;

import com.java.slms.dto.TeacherDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Subject;
import com.java.slms.model.Teacher;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.TeacherService;
import com.java.slms.util.Statuses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherServiceImpl implements TeacherService
{
    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;
    private final SubjectRepository subjectRepository;
    private final ClassEntityRepository classEntityRepository;

    @Override
    public TeacherDto createTeacher(TeacherDto teacherDto)
    {
        Teacher teacher = modelMapper.map(teacherDto, Teacher.class);
        Teacher savedTeacher = teacherRepository.save(teacher);
        return convertToDto(savedTeacher);
    }

    @Override
    public TeacherDto getTeacherById(Long id)
    {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        return convertToDto(teacher);
    }

    @Override
    public List<TeacherDto> getAllTeachers()
    {
        return teacherRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDto updateTeacher(Long id, TeacherDto teacherDto)
    {
        Teacher existingTeacher = teacherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));

        existingTeacher.setName(teacherDto.getName());
        existingTeacher.setEmail(teacherDto.getEmail());
        existingTeacher.setQualification(teacherDto.getQualification());

        if (teacherDto.getStatus() != null)
        {
            try
            {
                existingTeacher.setStatus(Statuses.valueOf(teacherDto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e)
            {
                throw new WrongArgumentException("Invalid status value: " + teacherDto.getStatus());
            }
        }

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);
        return convertToDto(updatedTeacher);
    }

    @Override
    public void deleteTeacher(Long id)
    {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        teacherRepository.delete(teacher);
        log.info("Deleted teacher with ID {}", id);
    }

    private TeacherDto convertToDto(Teacher teacher)
    {
        TeacherDto dto = modelMapper.map(teacher, TeacherDto.class);

        // 1. Populate subjectIds
        List<Long> subjectIds = Optional.ofNullable(teacher.getSubjects())
                .orElse(new ArrayList<>())
                .stream().map(Subject::getId)
                .collect(Collectors.toList());
        dto.setSubjectId(subjectIds);

        // 2. Populate classIds from related subjects (if not manually set)
        Set<Long> classIds = teacher.getSubjects() != null
                ? teacher.getSubjects().stream()
                .map(Subject::getClassEntity)
                .filter(Objects::nonNull)
                .map(ClassEntity::getId)
                .collect(Collectors.toSet())
                : Collections.emptySet();
        dto.setClassId(new ArrayList<>(classIds));

        // 3. Other metadata
        dto.setCreatedAt(teacher.getCreatedAt());
        dto.setUpdatedAt(teacher.getUpdatedAt());
        dto.setDeletedAt(teacher.getDeletedAt());
        dto.setStatus(teacher.getStatus() != null ? teacher.getStatus().toString() : null);

        return dto;
    }

}
