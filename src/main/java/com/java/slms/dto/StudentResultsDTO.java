package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultsDTO {
    private String studentPanNumber;
    private String studentName;
    private String className;
    private String section;
    private List<ExamResult> examResults;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamResult {
        private Long examId;
        private String examName;
        private String examDate;
        private List<SubjectScore> subjectScores;
        private Double totalMarks;
        private Double obtainedMarks;
        private Double percentage;
        private String overallGrade;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectScore {
        private Long subjectId;
        private String subjectName;
        private Double marks;
        private Double maxMarks;
        private String grade;
    }
}
