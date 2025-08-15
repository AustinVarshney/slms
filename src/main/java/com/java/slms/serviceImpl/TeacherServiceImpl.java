package com.java.slms.serviceImpl;

import com.java.slms.dto.TeacherDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Subject;
import com.java.slms.model.Teacher;
import com.java.slms.model.User;
import com.java.slms.repository.*;
import com.java.slms.service.TeacherService;
import com.java.slms.util.UserStatuses;
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
    private final FeeStaffRepository feeStaffRepository;

    @Override
    public TeacherDto createTeacher(TeacherDto teacherDto)
    {
        log.info("Creating teacher with email: {}", teacherDto.getEmail());

        if (teacherRepository.findByEmailIgnoreCase(teacherDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("Teacher already exists with email: " + teacherDto.getEmail());
        }

        Teacher teacher = modelMapper.map(teacherDto, Teacher.class);
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
        log.info("Fetching active teachers");
        List<User> activeUsers = userRepository.findByEmailIsNotNullAndEnabledTrue();
        List<String> emails = activeUsers.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        List<Teacher> teachers = teacherRepository.findByEmailIn(emails);
        return teachers.stream().map(this::convertToDto).toList();
    }

    @Override
    public TeacherDto updateTeacher(Long id, TeacherDto teacherDto)
    {
        log.info("Updating teacher with ID: {}", id);
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        modelMapper.map(teacherDto, existingTeacher);
        Teacher updatedTeacher = teacherRepository.save(existingTeacher);

        log.info("Teacher updated successfully: {}", id);
        return convertToDto(updatedTeacher);
    }


    @Override
    @Transactional
    public void deleteTeacher(Long id)
    {
        Teacher teacher = teacherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        teacherRepository.delete(teacher);
        log.info("Deleted teacher record with ID: {}", id);

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

        // 2. Populate classIds from related subjects
        Set<Long> classIds = teacher.getSubjects() != null
                ? teacher.getSubjects().stream()
                .map(Subject::getClassEntity)
                .filter(Objects::nonNull)
                .map(ClassEntity::getId)
                .collect(Collectors.toSet())
                : Collections.emptySet();
        dto.setClassId(new ArrayList<>(classIds));

        // 3. Status based on user
        userRepository.findByEmailIgnoreCase(teacher.getEmail()).ifPresent(user ->
        {
            dto.setStatus(user.isEnabled() ? UserStatuses.ACTIVE : UserStatuses.INACTIVE);
        });

        // 4. Timestamps
        dto.setCreatedAt(teacher.getCreatedAt());
        dto.setUpdatedAt(teacher.getUpdatedAt());
        dto.setDeletedAt(teacher.getDeletedAt());

        return dto;
    }
}
