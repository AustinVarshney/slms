package com.java.slms.controller;

import com.java.slms.dto.ExamDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
public class ExamController
{

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExamDto>> createExam(@RequestBody ExamDto examDto)
    {
        ExamDto saved = examService.createExam(examDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ExamDto>builder()
                        .data(saved)
                        .message("Exam created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<ExamDto>> getExamByName(@PathVariable String name)
    {
        ExamDto dto = examService.getExamByName(name);
        return ResponseEntity.ok(
                ApiResponse.<ExamDto>builder()
                        .data(dto)
                        .message("Exam fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<ExamDto>>> getExamsByClassName(@PathVariable Long classId)
    {
        List<ExamDto> list = examService.getExamsByClassId(classId);
        return ResponseEntity.ok(
                ApiResponse.<List<ExamDto>>builder()
                        .data(list)
                        .message("Exams for class: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{examId}/class/{classId}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long examId, @PathVariable Long classId)
    {
        examService.deleteExamByClass(examId, classId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .data(null)
                        .message("Exam deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}