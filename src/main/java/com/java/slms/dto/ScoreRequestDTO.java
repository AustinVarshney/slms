package com.java.slms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequestDTO
{
    private String studentPanNumber;
    private List<StudentScore> studentPanNumbers;
    private Long subjectId;
    private Long examId;
    private Long classId;
    private Double marks;
    private String grade;
}