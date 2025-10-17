package com.java.slms.service;

import com.java.slms.dto.CurrentDayAttendance;
import com.java.slms.dto.StudentRequestDto;
import com.java.slms.dto.StudentResponseDto;
import com.java.slms.dto.UpdateStudentInfo;
import com.java.slms.model.Student;
import com.java.slms.util.UserStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentService
{
    StudentResponseDto createStudent(StudentRequestDto studentRequestDto, Long schoolId);

    @Transactional
    void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status, Long schoolId);

    List<StudentResponseDto> getAllStudent(Long schoolId);

    List<StudentResponseDto> getActiveStudents(Long schoolId);

    StudentResponseDto getStudentByPAN(String pan, Long schoolId);

    Student getActiveStudentByPan(String pan, Long schoolId);

    StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo, Long schoolId);

    CurrentDayAttendance getStudentsPresentToday(Optional<Long> classId, Long schoolId);

    List<StudentResponseDto> getStudentsByClassId(Long classId, Long schoolId);

    Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(String panNumber, Long schoolId);

    Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(String panNumber, Long schoolId);

    List<Student> getStudentsBySchoolIdAndPanNumbers(Long schoolId, List<String> panNumbers);

    @Transactional
    void promoteStudentsToClass(List<String> panNumbers, Long classId, Long schoolId);
}
