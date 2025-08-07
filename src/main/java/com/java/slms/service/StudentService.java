package com.java.slms.service;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;

import java.util.List;

public interface StudentService
{
    StudentDto createStudent(StudentDto studentDto);

    List<StudentDto> getAllStudent();

    StudentDto getStudentByPAN(String pan);

    StudentDto updateStudent(String pan, StudentDto studentDto);

    StudentDto deleteStudent(String pan);

    List<StudentAttendance> getStudentsPresentToday();

    List<StudentAttendance> getStudentsPresentTodayByClass(Long classId);
}
