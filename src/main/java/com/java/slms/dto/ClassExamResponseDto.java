package com.java.slms.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ClassExamResponseDto
{
    private Long id;
    private Long classId;
    private Long schoolId;
    private String className;
    private Long examTypeId;
    private String examTypeName;
    private Integer maxMarks;
    private Integer passingMarks;
    private LocalDate examDate;
}
