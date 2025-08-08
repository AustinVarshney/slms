package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Student;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.StudentService;
import com.java.slms.util.Statuses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService
{
    private final ModelMapper modelMapper;
    private final StudentRepository studentRepository;
    private final ClassEntityRepository classEntityRepository;

    @Override
    public StudentDto createStudent(StudentDto studentDto)
    {
        log.info("Attempting to create student with PAN: {}", studentDto.getPanNumber());

        // Check if student already exists
        if (studentRepository.existsById(studentDto.getPanNumber()))
        {
            log.error("Student already exists with PAN: {}", studentDto.getPanNumber());
            throw new AlreadyExistException("Student already exists with PAN: " + studentDto.getPanNumber());
        }

        // Check if class exists
        if (!classEntityRepository.existsById(studentDto.getClassId()))
        {
            throw new ResourceNotFoundException("Class not found with id: " + studentDto.getClassId());
        }

        ClassEntity classEntity = classEntityRepository.findById(studentDto.getClassId()).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + studentDto.getClassId()));

        // Map and assign class
        Student student = modelMapper.map(studentDto, Student.class);
        student.setStatus(Statuses.ACTIVE);
        student.setCurrentClass(classEntity);

        Student savedStudent = studentRepository.save(student);
        log.info("Student created with PAN: {}", studentDto.getPanNumber());
        return modelMapper.map(savedStudent, StudentDto.class);
    }

    @Override
    public List<StudentDto> getAllStudent()
    {
        log.info("Fetching all students");
        List<Student> students = studentRepository.findAll();
        return students.stream().map(student -> modelMapper.map(student, StudentDto.class)).toList();
    }

    @Override
    public StudentDto getStudentByPAN(String pan)
    {
        log.info("Fetching student with PAN: {}", pan);

        Student fetchedStudent = studentRepository.findById(pan)
                .orElseThrow(() ->
                {
                    log.error("Student with PAN number '{}' was not found.", pan);
                    return new ResourceNotFoundException("Student with PAN number '" + pan + "' was not found.");
                });

        log.info("Student fetched successfully with PAN: {}", pan);
        return modelMapper.map(fetchedStudent, StudentDto.class);
    }

    @Override
    public StudentDto updateStudent(String pan, StudentDto studentDto)
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = studentRepository.findById(pan)
                .orElseThrow(() ->
                {
                    log.error("Student with PAN number '{}' was not found.", pan);
                    return new ResourceNotFoundException("Student with PAN number '" + pan + "' was not found.");
                });

        modelMapper.map(studentDto, fetchedStudent);
        fetchedStudent.setPanNumber(pan);
        Student updatedStudent = studentRepository.save(fetchedStudent);

        log.info("Student updated successfully with PAN: {}", pan);
        return modelMapper.map(updatedStudent, StudentDto.class);
    }

    @Override
    public StudentDto deleteStudent(String pan)
    {
        Student fetchedStudent = studentRepository.findById(pan)
                .orElseThrow(() ->
                {
                    log.error("Student with PAN number '{}' was not found.", pan);
                    return new ResourceNotFoundException("Student with PAN number '" + pan + "' was not found.");
                });

        // Check if the student's status is already INACTIVE
        if (fetchedStudent.getStatus() == Statuses.INACTIVE)
        {
            log.info("Student with PAN '{}' is already INACTIVE. No update needed.", pan);
            return modelMapper.map(fetchedStudent, StudentDto.class);
        }

        log.info("Student Deleted successfully with PAN: {}", pan);

        fetchedStudent.setStatus(Statuses.INACTIVE);
        fetchedStudent.setDeletedAt(new Date());

        return modelMapper.map(studentRepository.save(fetchedStudent), StudentDto.class);
    }

    @Override
    public List<StudentAttendance> getStudentsPresentToday()
    {
        List<Student> students = studentRepository.findStudentsPresentToday();
        return students.stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }

    @Override
    public List<StudentAttendance> getStudentsPresentTodayByClass(Long classId)
    {
        ClassEntity classEntity = classEntityRepository.findById(classId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + classId));

        List<Student> students = studentRepository.findStudentsPresentTodayByClassName(classId);
        return students.stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }
}
