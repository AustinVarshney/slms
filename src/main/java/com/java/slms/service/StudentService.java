package com.java.slms.service;

import com.java.slms.dto.*;
import com.java.slms.util.UserStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentService
{
    StudentResponseDto createStudent(StudentRequestDto studentRequestDto);

    boolean existsByPanNumber(String panNumber);

    @Transactional
    void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status);

    List<StudentResponseDto> getAllStudent();

    List<StudentResponseDto> getActiveStudents();

    StudentResponseDto getStudentByPAN(String pan);

    StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo);

    CurrentDayAttendance getStudentsPresentToday(Optional<Long> classId);

    List<StudentResponseDto> getStudentsByClassId(Long classId);
}
