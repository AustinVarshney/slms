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
public class ClassResultsDTO {
    private Long classId;
    private String className;
    private String section;
    private Long examId;
    private String examName;
    private List<SubjectInfo> subjects;
    private List<StudentResult> studentResults;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectInfo {
        private Long subjectId;
        private String subjectName;
        private Double maxMarks;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentResult {
        private String panNumber;
        private String studentName;
        private String rollNumber;
        private List<SubjectMarks> marks;
        private Double totalObtained;
        private Double totalMax;
        private Double percentage;
        private String overallGrade;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMarks {
        private Long subjectId;
        private String subjectName;
        private Double marks;
        private String grade;
    }
}
