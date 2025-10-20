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
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StaffLeaveAllowanceRepository staffLeaveAllowanceRepository;
    private final SchoolRepository schoolRepository;
    private final StaffRepository staffRepository;
    private final ClassEntityRepository classEntityRepository;

    @Override
    public TeacherDto createTeacher(TeacherDto teacherDto, Long schoolId)
    {
        log.info("Creating teacher with email: {}", teacherDto.getEmail());

        if (teacherRepository.findByEmailIgnoreCaseAndSchoolId(teacherDto.getEmail(), schoolId).isPresent())
        {
            throw new AlreadyExistException("A teacher already exists with the email: " + teacherDto.getEmail() +
                    ". If you believe this is an issue with expired credentials, please try re-register.");
        }

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        Teacher teacher = modelMapper.map(teacherDto, Teacher.class);
        teacher.setStatus(UserStatus.ACTIVE);
        teacher.setSchool(school);
        Teacher savedTeacher = teacherRepository.save(teacher);

        // Create leave allowance for the teacher in the active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school ID: " + schoolId));

        Staff staff = new Staff();
        staff.setStaffType(RoleEnum.ROLE_TEACHER);
        staff.setSchool(school);
        staff.setEmail(teacher.getEmail());
        Staff savedStaff = staffRepository.save(staff);

        // Create staff leave allowance
        StaffLeaveAllowance staffLeaveAllowance = new StaffLeaveAllowance();
        staffLeaveAllowance.setSession(activeSession);
        staffLeaveAllowance.setStaff(savedStaff);
        staffLeaveAllowance.setSchool(school);
        Integer allowedLeaves = teacherDto.getAllowedLeaves();
        staffLeaveAllowance.setAllowedLeaves(allowedLeaves != null ? allowedLeaves : 10);
        staffLeaveAllowanceRepository.save(staffLeaveAllowance);

        log.info("Teacher created with ID: {}", savedTeacher.getId());
        return convertToDto(savedTeacher, schoolId);
    }

    @Override
    public TeacherDto getTeacherById(Long id, Long schoolId)
    {
        log.info("Fetching teacher with ID: {}", id);
        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        return convertToDto(teacher, schoolId);
    }

    @Override
    public List<TeacherDto> getAllTeachers(Long schoolId)
    {
        log.info("Fetching all teachers");
        return teacherRepository.findAllBySchoolId(schoolId).stream()
                .map(teacher -> convertToDto(teacher, schoolId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDto> getActiveTeachers(Long schoolId)
    {
        List<Teacher> teachers = teacherRepository.findAllBySchoolIdAndActive(schoolId);
        return teachers.stream()
                .map(teacher -> convertToDto(teacher, schoolId))
                .collect(Collectors.toList());
    }

    @Override
    public Teacher getActiveTeacherByEmail(String email, Long schoolId)
    {
        log.info("Fetching active teacher by email: {}", email);

        return teacherRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + email));
    }

    @Override
    @Transactional
    public void inActiveTeacher(Long id, Long schoolId)
    {
        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolId(id, schoolId).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        
        // If already inactive, just return (idempotent operation)
        if (teacher.getStatus().equals(UserStatus.INACTIVE)) {
            log.info("Teacher with ID {} is already inactive, skipping deactivation", id);
            return;
        }

        // DO NOT remove teacher from subjects - keep the assignment
        // This allows the teacher to be reactivated with the same subjects
        // The frontend/backend should check teacher status when needed
        
        teacher.setStatus(UserStatus.INACTIVE);
        teacherRepository.save(teacher);
        EntityFetcher.removeRoleFromUser(teacher.getUser().getId(), RoleEnum.ROLE_TEACHER, userRepository);

    }

    @Override
    @Transactional
    public void activateTeacher(Long id, Long schoolId)
    {
        log.info("Activating teacher with ID: {} for school ID: {}", id, schoolId);
        
        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolId(id, schoolId).orElseThrow(() ->
                new ResourceNotFoundException("Teacher not found with ID: " + id));
        
        // If already active, just return (idempotent operation)
        if (teacher.getStatus().equals(UserStatus.ACTIVE)) {
            log.info("Teacher with ID {} is already active, skipping activation", id);
            return;
        }

        // Set teacher status to active
        teacher.setStatus(UserStatus.ACTIVE);
        teacherRepository.save(teacher);
        
        // Restore ROLE_TEACHER to user AND enable the user account
        User user = teacher.getUser();
        if (user != null) {
            // Enable the user account
            user.setEnabled(true);
            
            // Check if user already has ROLE_TEACHER
            if (!user.getRoles().contains(RoleEnum.ROLE_TEACHER)) {
                user.getRoles().add(RoleEnum.ROLE_TEACHER);
            }
            userRepository.save(user);
            log.info("Enabled user account and added ROLE_TEACHER back to user with ID: {}", user.getId());
        }
        
        log.info("Teacher with ID {} has been successfully activated", id);
    }

    @Override
    public TeacherDto getTeacherByEmail(String email, Long schoolId)
    {
        log.info("Fetching teacher with email: {}", email);
        Teacher teacher = teacherRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + email));

        return convertToDto(teacher, schoolId);
    }


    private TeacherDto convertToDto(Teacher teacher, Long schoolId)
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

        // 3. Add class where teacher is assigned as class teacher
        List<ClassEntity> classTeacherClasses = classEntityRepository.findAllByClassTeacher_Id(teacher.getId());
        classes.addAll(classTeacherClasses);

        List<Long> classIds = classes.stream()
                .map(ClassEntity::getId)
                .collect(Collectors.toList());
        dto.setClassId(classIds);

        List<String> classNames = classes.stream()
                .map(ClassEntity::getClassName)
                .collect(Collectors.toList());
        dto.setClassName(classNames);

        Optional<Session> activeSessionOpt = sessionRepository.findBySchoolIdAndActiveTrue(schoolId);

        if (activeSessionOpt.isPresent())
        {
            Session activeSession = activeSessionOpt.get();

            Staff staff = staffRepository
                    .findByEmailAndSchoolIdIgnoreCase(
                            teacher.getEmail(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff record not found"));

            Optional<StaffLeaveAllowance> allowanceOpt =
                    staffLeaveAllowanceRepository.findByStaffAndSessionAndSchoolId(staff, activeSession, schoolId);

            allowanceOpt.ifPresent(allowance -> dto.setAllowedLeaves(allowance.getAllowedLeaves()));

        }
        // 4. Timestamps
        dto.setCreatedAt(teacher.getCreatedAt());
        dto.setUpdatedAt(teacher.getUpdatedAt());
        dto.setDeletedAt(teacher.getDeletedAt());

        return dto;
    }

}
