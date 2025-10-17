package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClassExamEntry
{
    private Long classId;
    private Integer maxMarks;
    private Integer passingMarks;
    private LocalDate examDate;
}
