package com.java.slms.service;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;

import java.util.List;

public interface ScoreService
{
    ScoreResponseDTO createScore(ScoreRequestDTO scoreDto);

    List<ScoreResponseDTO> getScoresByStudentPan(String panNumber);

    List<ScoreResponseDTO> getScoresByExamIdAndClassId(Long examId, Long classId);

    List<ScoreResponseDTO> getScoresByStudentPanAndExamName(String panNumber, String examName);

    List<ScoreResponseDTO> getScoresByStudentPanAndSubjectName(String panNumber, String subjectName);

    List<ScoreResponseDTO> getScoresByExamNameAndSubjectNameAndClassName(String examName, String subjectName, String className);

}