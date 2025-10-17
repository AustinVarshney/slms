package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectMarksDto
{
    private String subjectName;
    private Integer marksObtained;
    private Integer maxMarks;
    private Integer passingMarks;
    private boolean passed;
}
