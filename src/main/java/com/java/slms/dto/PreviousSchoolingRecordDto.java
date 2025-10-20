package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousSchoolingRecordDto {
    private Long sessionId;
    private String sessionName;
    private String className;
    private String section;
    private Integer passingYear;
    private String status; // "COMPLETED", "TRANSFERRED", "CURRENT"
    
    // Academic Performance
    private Double overallPercentage;
    private String overallGrade;
    private Integer totalPresent;
    private Integer totalAbsent;
    private Double attendancePercentage;
    
    // Exam Results Summary
    private List<ExamResultSummary> examResults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamResultSummary {
        private String examName;
        private String examDate;
        private Double percentage;
        private String grade;
        private Integer obtainedMarks;
        private Integer totalMarks;
    }
}
