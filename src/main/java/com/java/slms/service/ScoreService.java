package com.java.slms.service;

import com.java.slms.dto.*;

import java.util.List;

public interface ScoreService
{
    List<ScoreResponseDTO> createScoresOfStudents(ScoreRequestDTO scoreDto);

    List<ScoreResponseDTO> getScoresByStudentPan(String panNumber);

    List<ScoreResponseDTO> getScoresByExamIdAndClassId(Long examId, Long classId);

    ScoreResponseDTO updateScoreByExamIdClassIdSubjectIdAndPanNumber(Long examId, Long classId, Long subjectId, String PanNumber, StudentScore studentScore);

    void deleteScoreByExamIdClassIdSubjectIdAndPanNumber(Long examId, Long classId, Long subjectId, String PanNumber);

    List<ScoreResponseDTO> getScoresByStudentPanAndExamName(String panNumber, Long examId);

    List<ScoreResponseDTO> getScoresByStudentPanAndSubjectName(String panNumber, String subjectName);

    List<ScoreResponseDTO> getScoresByStudentPanAndExamId(String panNumber, Long examId);

    List<ScoreResponseDTO> getScoresByExamNameAndSubjectNameAndClassName(String examName, String subjectName, String className);
    
    // New methods for bulk results management
    List<ScoreResponseDTO> bulkUpdateScores(BulkScoreUpdateDTO bulkScoreUpdateDTO);
    
    ClassResultsDTO getClassResultsForExam(Long classId, Long examId);
    
    StudentResultsDTO getStudentAllResults(String panNumber);
    
    List<ScoreResponseDTO> getScoresByClassIdSubjectIdAndExamId(Long classId, Long subjectId, Long examId);

}