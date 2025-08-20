package com.java.slms.service;

import com.java.slms.dto.StudentRequestDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.dto.StudentResponseDto;
import com.java.slms.dto.UpdateStudentInfo;
import com.java.slms.util.UserStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentService
{
    StudentResponseDto createStudent(StudentRequestDto studentRequestDto);

    @Transactional
    void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status);

    List<StudentResponseDto> getAllStudent();

    List<StudentResponseDto> getActiveStudents();

    StudentResponseDto getStudentByPAN(String pan);

    StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo);

    List<StudentAttendance> getStudentsPresentToday();

    List<StudentAttendance> getStudentsPresentTodayByClass(Long classId);

    void deleteStudentByPan(String panNumber);

    List<StudentResponseDto> getStudentsByClassId(Long classId);
}
