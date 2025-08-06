package com.java.slms.service;

import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;

import java.util.List;

public interface SubjectService
{
    SubjectDto addSubject(SubjectDto subjectDto);

    List<SubjectDto> getAllSubjects();

    SubjectDto getSubjectById(Long id);

    void deleteSubject(Long subjectId, Long classId);

    SubjectDto updateSubjectById(Long subjectId, SubjectDto subjectDto);

    List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto);

    List<SubjectDto> getSubjectsByClassId(Long classId);

}