package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentMarksResponseDto
{
    private String studentName;
    private String panNumber;
    private String className;
    private String sessionName;
    private List<ExamResultDto> examResults;
}