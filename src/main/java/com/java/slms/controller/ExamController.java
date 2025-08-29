package com.java.slms.controller;

import com.java.slms.dto.ExamDto;
import com.java.slms.payload.RestResponse;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
public class ExamController
{

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<RestResponse<ExamDto>> createExam(@RequestBody ExamDto examDto)
    {
        ExamDto saved = examService.createExam(examDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<ExamDto>builder()
                        .data(saved)
                        .message("Exam created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/{name}")
    public ResponseEntity<RestResponse<ExamDto>> getExamByName(@PathVariable String name)
    {
        ExamDto dto = examService.getExamByName(name);
        return ResponseEntity.ok(
                RestResponse.<ExamDto>builder()
                        .data(dto)
                        .message("Exam fetched")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<RestResponse<List<ExamDto>>> getExamsByClassName(@PathVariable Long classId)
    {
        List<ExamDto> list = examService.getExamsByClassId(classId);
        return ResponseEntity.ok(
                RestResponse.<List<ExamDto>>builder()
                        .data(list)
                        .message("Exams for class: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{examId}/class/{classId}")
    public ResponseEntity<RestResponse<Void>> deleteExam(@PathVariable Long examId, @PathVariable Long classId)
    {
        examService.deleteExamByClass(examId, classId);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .data(null)
                        .message("Exam deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}