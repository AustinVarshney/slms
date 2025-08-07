package com.java.slms.service;

import com.java.slms.dto.TeacherDto;

import java.util.List;

public interface TeacherService
{
    TeacherDto createTeacher(TeacherDto teacherDto);

    TeacherDto getTeacherById(Long id);

    List<TeacherDto> getAllTeachers();

    TeacherDto updateTeacher(Long id, TeacherDto teacherDto);

    void deleteTeacher(Long id);

}
