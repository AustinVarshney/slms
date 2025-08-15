package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Student;
import com.java.slms.model.User;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.StudentService;
import com.java.slms.util.CommonUtil;
import com.java.slms.util.UserStatuses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService
{

    private final ModelMapper modelMapper;
    private final StudentRepository studentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;

    @Override
    public StudentDto createStudent(StudentDto studentDto)
    {
        log.info("Attempting to create student with PAN: {}", studentDto.getPanNumber());

        if (studentRepository.existsById(studentDto.getPanNumber()))
        {
            throw new AlreadyExistException("Student already exists with PAN: " + studentDto.getPanNumber());
        }

        ClassEntity classEntity = classEntityRepository.findById(studentDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + studentDto.getClassId()));

        User user = userRepository.findByPanNumberIgnoreCase(studentDto.getPanNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student User not found with PAN: " + studentDto.getPanNumber()));

        Student student = modelMapper.map(studentDto, Student.class);
        student.setCurrentClass(classEntity);
        student.setUser(user);

        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with PAN: {}", studentDto.getPanNumber());

        return convertToDto(savedStudent);
    }

    @Override
    public List<StudentDto> getAllStudent()
    {
        log.info("Fetching all students");
        return studentRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<StudentDto> getActiveStudents()
    {
        log.info("Fetching students with the status: ACTIVE");

        List<User> activeUsers = userRepository.findByPanNumberIsNotNullAndEnabledTrue();
        List<String> panNumbers = activeUsers.stream()
                .map(User::getPanNumber)
                .filter(Objects::nonNull)
                .toList();

        List<Student> students = studentRepository.findByPanNumberIn(panNumbers);

        return students.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public StudentDto getStudentByPAN(String pan)
    {
        log.info("Fetching student with PAN: {}", pan);
        Student fetchedStudent = CommonUtil.fetchStudentByPan(studentRepository, pan);

        StudentDto studentDto = convertToDto(fetchedStudent);

        log.info("Student fetched successfully with PAN: {}", pan);
        return studentDto;
    }

    @Override
    public StudentDto updateStudent(String pan, StudentDto studentDto)
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = CommonUtil.fetchStudentByPan(studentRepository, pan);
        modelMapper.map(studentDto, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        Student updatedStudent = studentRepository.save(fetchedStudent);

        log.info("Student updated successfully with PAN: {}", pan);
        return convertToDto(updatedStudent);
    }


    @Override
    public List<StudentAttendance> getStudentsPresentToday()
    {
        log.info("Fetching today's present students");
        return studentRepository.findStudentsPresentToday().stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }

    @Override
    public List<StudentAttendance> getStudentsPresentTodayByClass(Long classId)
    {
        log.info("Fetching today's present students for class ID: {}", classId);
        CommonUtil.fetchClassEntityByClassId(classEntityRepository, classId);

        return studentRepository.findStudentsPresentTodayByClassName(classId).stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }

    @Override
    public void deleteStudentByPan(String panNumber)
    {
        log.info("Deleting student and user with PAN: {}", panNumber);

        Student student = CommonUtil.fetchStudentByPan(studentRepository, panNumber);

        User user = userRepository.findByPanNumberIgnoreCase(panNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with PAN: " + panNumber));

        studentRepository.delete(student);
        log.info("Deleted student with PAN: {}", panNumber);

    }


    @Override
    public List<StudentDto> getStudentsByClassId(Long classId)
    {
        log.info("Fetching students for class ID: {}", classId);
        CommonUtil.fetchClassEntityByClassId(classEntityRepository, classId);

        return studentRepository.findByCurrentClass_Id(classId).stream()
                .filter(student -> student.getUser().isEnabled())
                .map(this::convertToDto)
                .toList();
    }


    // Common conversion method used across all methods
    private StudentDto convertToDto(Student student)
    {
        StudentDto dto = modelMapper.map(student, StudentDto.class);
        boolean isActive = student.getUser() != null && student.getUser().isEnabled();
        dto.setStatus(isActive ? UserStatuses.ACTIVE : UserStatuses.INACTIVE);
        return dto;
    }
}
