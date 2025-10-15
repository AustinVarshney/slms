package com.java.slms.controller;

import com.java.slms.dto.BulkScoreUpdateDTO;
import com.java.slms.dto.ClassResultsDTO;
import com.java.slms.dto.ScoreResponseDTO;
import com.java.slms.dto.StudentResultsDTO;
import com.java.slms.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class ResultsController {

    @Autowired
    private ScoreService scoreService;

    /**
     * Bulk update scores for multiple students in a class for a specific exam and subject
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<List<ScoreResponseDTO>> bulkUpdateScores(@RequestBody BulkScoreUpdateDTO bulkScoreUpdateDTO) {
        try {
            List<ScoreResponseDTO> updatedScores = scoreService.bulkUpdateScores(bulkScoreUpdateDTO);
            return ResponseEntity.ok(updatedScores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all results for a specific class and exam
     * This shows all students' scores across all subjects for the specified exam
     */
    @GetMapping("/class/{classId}/exam/{examId}")
    public ResponseEntity<ClassResultsDTO> getClassResultsForExam(
            @PathVariable Long classId,
            @PathVariable Long examId) {
        try {
            ClassResultsDTO results = scoreService.getClassResultsForExam(classId, examId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all exam results for a specific student across all exams
     * This shows a student's complete academic record
     */
    @GetMapping("/student/{panNumber}")
    public ResponseEntity<StudentResultsDTO> getStudentAllResults(@PathVariable String panNumber) {
        try {
            StudentResultsDTO results = scoreService.getStudentAllResults(panNumber);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get scores filtered by class, subject, and exam
     * This is useful for teachers to view/edit scores for a specific subject in a specific exam
     */
    @GetMapping("/class/{classId}/subject/{subjectId}/exam/{examId}")
    public ResponseEntity<List<ScoreResponseDTO>> getScoresByClassSubjectAndExam(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @PathVariable Long examId) {
        try {
            List<ScoreResponseDTO> scores = scoreService.getScoresByClassIdSubjectIdAndExamId(classId, subjectId, examId);
            return ResponseEntity.ok(scores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
