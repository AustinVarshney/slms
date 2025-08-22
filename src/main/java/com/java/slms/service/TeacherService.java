package com.java.slms.service;

import com.java.slms.dto.TeacherDto;

import java.util.List;

public interface TeacherService
{
    TeacherDto createTeacher(TeacherDto teacherDto);

    TeacherDto getTeacherById(Long id);

    List<TeacherDto> getAllTeachers();

    List<TeacherDto> getActiveTeachers();

    void inActiveTeacher(Long id);

    TeacherDto getTeacherByEmail(String email);

}
