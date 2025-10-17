package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentsMarksDto
{
    private Integer marksObtained;

    private Integer maxMarks;

    private Integer passingMarks;

    private Long subjectId;

    private String subjectName;

    private Long classId;

    private Long className;

    private String examType;
}
