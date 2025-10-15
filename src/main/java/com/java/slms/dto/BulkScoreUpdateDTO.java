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
public class BulkScoreUpdateDTO {
    private Long examId;
    private Long classId;
    private Long subjectId;
    private List<StudentScoreEntry> scores;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentScoreEntry {
        private String studentPanNumber;
        private String studentName;
        private Double marks;
        private String grade;
    }
}
