package com.java.slms.serviceImpl;

import com.java.slms.dto.TeacherDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.TeacherService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.RoleEnum;
import com.java.slms.util.UserStatus;
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
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final NonTeachingStaffRepository nonTeachingStaffRepository;

    @Override
    public TeacherDto createTeacher(TeacherDto teacherDto)
    {
        log.info("Creating teacher with email: {}", teacherDto.getEmail());

        if (teacherRepository.findByEmailIgnoreCase(teacherDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("Teacher already exists with email: " + teacherDto.getEmail());
        }

        Teacher teacher = modelMapper.map(teacherDto, Teacher.class);
        teacher.setStatus(UserStatus.ACTIVE);
        Teacher savedTeacher = teacherRepository.save(teacher);

        log.info("Teacher created with ID: {}", savedTeacher.getId());
        return convertToDto(savedTeacher);
    }

    @Override
    public TeacherDto getTeacherById(Long id)
    {
        log.info("Fetching teacher with ID: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        return convertToDto(teacher);
    }

    @Override
    public List<TeacherDto> getAllTeachers()
    {
        log.info("Fetching all teachers");
        return teacherRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDto> getActiveTeachers()
    {
        List<Teacher> teachers = teacherRepository.findByStatus(UserStatus.ACTIVE);
        return teachers.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional
    public void inActiveTeacher(Long id)
    {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        if (teacher.getStatus().equals(UserStatus.INACTIVE))
            throw new AlreadyExistException("Teacher Already inactive");

        List<Subject> assignedSubjects = teacher.getSubjects();
        for (Subject subject : assignedSubjects)
        {
            subject.setTeacher(null);
        }
        subjectRepository.saveAll(assignedSubjects);

        teacher.setStatus(UserStatus.INACTIVE);
        teacherRepository.save(teacher);
        EntityFetcher.removeRoleFromUser(teacher.getUser().getId(), RoleEnum.ROLE_TEACHER, userRepository);

    }

    @Override
    public TeacherDto getTeacherByEmail(String email)
    {
        log.info("Fetching teacher with email: {}", email);
        Teacher teacher = teacherRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + email));

        return convertToDto(teacher);
    }

    private TeacherDto convertToDto(Teacher teacher)
    {
        TeacherDto dto = modelMapper.map(teacher, TeacherDto.class);

        // 1. Populate subjectIds and subjectNames
        List<Subject> subjects = Optional.ofNullable(teacher.getSubjects()).orElse(new ArrayList<>());

        List<Long> subjectIds = subjects.stream()
                .map(Subject::getId)
                .collect(Collectors.toList());
        dto.setSubjectId(subjectIds);

        List<String> subjectNames = subjects.stream()
                .map(Subject::getSubjectName)
                .collect(Collectors.toList());
        dto.setSubjectName(subjectNames);

        // 2. Populate classIds and classNames from related subjects
        Set<ClassEntity> classes = subjects.stream()
                .map(Subject::getClassEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> classIds = classes.stream()
                .map(ClassEntity::getId)
                .collect(Collectors.toList());
        dto.setClassId(classIds);

        List<String> classNames = classes.stream()
                .map(ClassEntity::getClassName)  // assuming getClassName() returns class name
                .collect(Collectors.toList());
        dto.setClassName(classNames);

        // 4. Timestamps
        dto.setCreatedAt(teacher.getCreatedAt());
        dto.setUpdatedAt(teacher.getUpdatedAt());
        dto.setDeletedAt(teacher.getDeletedAt());

        return dto;
    }

}
