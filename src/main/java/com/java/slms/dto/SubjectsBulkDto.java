package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubjectsBulkDto
{
    private Long classId;
    private List<SpecificSubject> subjects;
}