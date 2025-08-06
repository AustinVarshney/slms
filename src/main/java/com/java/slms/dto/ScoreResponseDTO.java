package com.java.slms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponseDTO
{
    private Long id;
    private String studentPanNumber;
    private Long subjectId;
    private String subjectName;
    private Long examId;
    private String examName;
    private Long classId;
    private String className;
    private Double marks;
    private String grade;
}
