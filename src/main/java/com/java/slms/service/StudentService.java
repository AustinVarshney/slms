package com.java.slms.service;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;

import java.util.List;

public interface StudentService
{
    StudentDto createStudent(StudentDto studentDto);

    List<StudentDto> getAllStudent();

    List<StudentDto> getActiveStudents();

    StudentDto getStudentByPAN(String pan);

    StudentDto updateStudent(String pan, StudentDto studentDto);

    List<StudentAttendance> getStudentsPresentToday();

    List<StudentAttendance> getStudentsPresentTodayByClass(Long classId);

    void deleteStudentByPan(String panNumber);

    List<StudentDto> getStudentsByClassId(Long classId);
}
