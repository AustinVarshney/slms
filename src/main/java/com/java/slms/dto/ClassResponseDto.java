package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponseDto
{
    private Long id;
    private String className;
    private Long sessionId;
    private String sessionName;
    private Double feesAmount;
    private List<StudentRequestDto> students;
    private List<SubjectDto> subjects;
    private List<ExamDto> exams;
    private int totalStudents;

}
