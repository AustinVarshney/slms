package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequestDTO
{
    private String studentPanNumber;
    private Long subjectId;
    private Long examId;
    private Long classId;
    private Double marks;
    private String grade;
}