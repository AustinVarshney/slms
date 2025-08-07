package com.java.slms.controller;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;
import com.java.slms.dto.StudentScore;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController
{

    private final ScoreService scoreService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<ScoreResponseDTO>>> createScoresOfStudents(@RequestBody ScoreRequestDTO scoreDto)
    {
        List<ScoreResponseDTO> saved = scoreService.createScoresOfStudents(scoreDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<List<ScoreResponseDTO>>builder()
                        .data(saved)
                        .message("Score created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }


    @GetMapping("/pan/{panNumber}")
    public ResponseEntity<ApiResponse<List<ScoreResponseDTO>>> getScoresByStudentPan(@PathVariable String panNumber)
    {
        List<ScoreResponseDTO> scores = scoreService.getScoresByStudentPan(panNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<List<ScoreResponseDTO>>builder()
                        .data(scores)
                        .message("Score Fetched By PanNumber")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/exam/{examId}/class/{classId}")
    public ResponseEntity<ApiResponse<List<ScoreResponseDTO>>> getScoresByExamIdAndClassId(
            @PathVariable Long examId,
            @PathVariable Long classId)
    {

        List<ScoreResponseDTO> scores = scoreService.getScoresByExamIdAndClassId(examId, classId);

        return ResponseEntity.ok(
                ApiResponse.<List<ScoreResponseDTO>>builder()
                        .data(scores)
                        .message("Scores fetched By ClassName and PanNumber")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/exam/{examId}/class/{classId}/subject/{subjectId}/pan/{panNumber}")
    public ResponseEntity<ApiResponse<ScoreResponseDTO>> updateScoreByExamIdClassIdSubjectIdAndPanNumber(
            @PathVariable Long examId,
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @PathVariable String panNumber,
            @RequestBody StudentScore studentScore
    )
    {

        ScoreResponseDTO scores = scoreService.updateScoreByExamIdClassIdSubjectIdAndPanNumber(examId, classId, subjectId, panNumber, studentScore);

        return ResponseEntity.ok(
                ApiResponse.<ScoreResponseDTO>builder()
                        .data(scores)
                        .message("Scores fetched by Exam, Class, Subject, and PAN")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/exam/{examId}/class/{classId}/subject/{subjectId}/pan/{panNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteScore(
            @PathVariable Long examId,
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @PathVariable String panNumber) {

        scoreService.deleteScoreByExamIdClassIdSubjectIdAndPanNumber(examId, classId, subjectId, panNumber);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Score deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
