package com.java.slms.service;

import com.java.slms.dto.TeacherDto;
import com.java.slms.model.Teacher;

import java.util.List;

public interface TeacherService
{
    TeacherDto createTeacher(TeacherDto teacherDto);

    TeacherDto getTeacherById(Long id);

    List<TeacherDto> getAllTeachers();

    List<TeacherDto> getActiveTeachers();

    Teacher getActiveTeacherByEmail(String email);

    void inActiveTeacher(Long id);
    
    void activateTeacher(Long id);

    TeacherDto getTeacherByEmail(String email);

}
