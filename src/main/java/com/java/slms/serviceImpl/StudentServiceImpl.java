package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.model.Student;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService
{
    private final ModelMapper modelMapper;
    private final StudentRepository studentRepository;

    @Override
    public StudentDto createStudent(StudentDto studentDto)
    {
        log.info("Attempting to create student with PAN: {}", studentDto.getPanNumber());

        if (studentRepository.existsById(studentDto.getPanNumber()))
        {
            log.error("Student already exists with PAN: {}", studentDto.getPanNumber());
            throw new AlreadyExistException("Student already exists with PAN: " + studentDto.getPanNumber());
        }

        Student savedStudent = studentRepository.save(modelMapper.map(studentDto, Student.class));
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
}
