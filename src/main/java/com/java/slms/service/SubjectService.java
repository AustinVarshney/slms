package com.java.slms.service;

import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;

import java.util.List;

public interface SubjectService
{
    SubjectDto addSubject(SubjectDto subjectDto);

    List<SubjectDto> getAllSubjects();

    SubjectDto getSubjectByName(String name);

    void deleteSubject(String name);

    SubjectDto updateSubject(String name, SubjectDto subjectDto);

    List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto);

}