package com.java.slms.service;

import com.java.slms.dto.ClassEntityDto;

import java.util.List;

public interface ClassEntityService
{
    ClassEntityDto addClass(ClassEntityDto classEntityDto);

    List<ClassEntityDto> getAllClass();

    List<ClassEntityDto> getClassWithSections();

    ClassEntityDto getClassByClassId(Long id);

    void deleteClassById(Long id);

    ClassEntityDto updateClassNameById(Long id, ClassEntityDto classEntityDto);
}
