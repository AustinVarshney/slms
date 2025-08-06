package com.java.slms.dto;

import lombok.Data;

@Data
public class SubjectDto
{
    private Long id;
    private String subjectName;
    private Long classId;
    private String className;
}
