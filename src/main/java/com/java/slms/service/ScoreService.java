package com.java.slms.service;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;
import com.java.slms.dto.StudentScore;

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

}