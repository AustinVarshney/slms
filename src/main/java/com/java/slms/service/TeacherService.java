package com.java.slms.service;

import com.java.slms.dto.TeacherDto;
import com.java.slms.model.Teacher;

import java.util.List;

public interface TeacherService
{

    // Create a new teacher
    TeacherDto createTeacher(TeacherDto teacherDto, Long schoolId);

    // Get teacher by ID
    TeacherDto getTeacherById(Long id, Long schoolId);

    // Get all teachers for a particular school
    List<TeacherDto> getAllTeachers(Long schoolId);

    // Get all active teachers for a particular school
    List<TeacherDto> getActiveTeachers(Long schoolId);

    // Get active teacher by email for a specific school
    Teacher getActiveTeacherByEmail(String email, Long schoolId);

    // Inactivate teacher by ID
    void inActiveTeacher(Long id, Long schoolId);

    // Reactivate teacher by ID
    void activateTeacher(Long id, Long schoolId);

    // Get teacher by email for a specific school
    TeacherDto getTeacherByEmail(String email, Long schoolId);

}
