package com.java.slms.service;

import com.java.slms.dto.ClassEntityDto;

import java.util.List;

public interface ClassEntityService
{
    ClassEntityDto addClass(ClassEntityDto classEntityDto);

    List<ClassEntityDto> getAllClass();

    List<ClassEntityDto> getClassWithSections();

    ClassEntityDto getClassByName(String name);

    void deleteClass(String name);

    ClassEntityDto updateClassName(String name, ClassEntityDto classEntityDto);
}
