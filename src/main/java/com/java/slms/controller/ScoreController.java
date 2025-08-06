package com.java.slms.controller;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;
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
    public ResponseEntity<ApiResponse<ScoreResponseDTO>> createScore(@RequestBody ScoreRequestDTO scoreDto)
    {
        ScoreResponseDTO saved = scoreService.createScore(scoreDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ScoreResponseDTO>builder()
                        .data(saved)
                        .message("Score created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/pan/{panNumber}")
    public ResponseEntity<ApiResponse<List<ScoreResponseDTO>>> createScore(@PathVariable String panNumber)
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
    public ResponseEntity<ApiResponse<List<ScoreResponseDTO>>> getScoresByExamNameAndClassName(
            @PathVariable Long examId,
            @PathVariable Long classId) {

        List<ScoreResponseDTO> scores = scoreService.getScoresByExamIdAndClassId(examId, classId);

        return ResponseEntity.ok(
                ApiResponse.<List<ScoreResponseDTO>>builder()
                        .data(scores)
                        .message("Scores fetched By ClassName and PanNumber")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


}
