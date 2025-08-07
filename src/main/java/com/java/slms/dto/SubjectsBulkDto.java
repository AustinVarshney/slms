package com.java.slms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubjectsBulkDto
{
    private Long classId;
    private List<SpecificSubject> subjects;
}