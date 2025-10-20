package com.java.slms.controller;

import com.java.slms.dto.ExamDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Tag(name = "Exam Management")
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
public class ExamController
{
    private final ExamService examService;

    @Operation(summary = "Get all exams for a specific class")
    @GetMapping("/class/{classId}")
    public ResponseEntity<RestResponse<List<ExamDto>>> getExamsByClass(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching exams for class: {} in school: {}", classId, schoolId);
        List<ExamDto> exams = examService.getExamsByClass(classId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<ExamDto>>builder()
                        .data(exams)
                        .message("Found " + exams.size() + " exam(s)")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get all exams for a specific classExam")
    @GetMapping("/class-exam/{classExamId}")
    public ResponseEntity<RestResponse<List<ExamDto>>> getExamsByClassExam(
            @PathVariable Long classExamId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching exams for classExam: {} in school: {}", classExamId, schoolId);
        List<ExamDto> exams = examService.getExamsByClassExam(classExamId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<ExamDto>>builder()
                        .data(exams)
                        .message("Found " + exams.size() + " exam(s)")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get all exams for a specific subject")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<RestResponse<List<ExamDto>>> getExamsBySubject(
            @PathVariable Long subjectId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching exams for subject: {} in school: {}", subjectId, schoolId);
        List<ExamDto> exams = examService.getExamsBySubject(subjectId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<ExamDto>>builder()
                        .data(exams)
                        .message("Found " + exams.size() + " exam(s)")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get exam by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ExamDto>> getExamById(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching exam: {} in school: {}", id, schoolId);
        ExamDto exam = examService.getExamById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<ExamDto>builder()
                        .data(exam)
                        .message("Exam found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Create a new exam")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<RestResponse<ExamDto>> createExam(
            @RequestBody ExamDto examDto,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Creating exam: {} for school: {}", examDto.getName(), schoolId);
        ExamDto created = examService.createExam(examDto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<ExamDto>builder()
                        .data(created)
                        .message("Exam created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(summary = "Update an existing exam")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ExamDto>> updateExam(
            @PathVariable Long id,
            @RequestBody ExamDto examDto,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Updating exam: {} in school: {}", id, schoolId);
        ExamDto updated = examService.updateExam(id, examDto, schoolId);
        return ResponseEntity.ok(
                RestResponse.<ExamDto>builder()
                        .data(updated)
                        .message("Exam updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Delete an exam")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<String>> deleteExam(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Deleting exam: {} in school: {}", id, schoolId);
        examService.deleteExam(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Exam deleted successfully")
                        .message("Exam deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get all exams")
    @GetMapping
    public ResponseEntity<RestResponse<List<ExamDto>>> getAllExams(
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching all exams for school: {}", schoolId);
        List<ExamDto> exams = examService.getAllExams(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<ExamDto>>builder()
                        .data(exams)
                        .message("Found " + exams.size() + " exam(s)")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Sync/Create missing exams for a class")
    @PostMapping("/sync/class/{classId}")
    public ResponseEntity<RestResponse<String>> syncExamsForClass(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Syncing exams for class: {} in school: {}", classId, schoolId);
        int created = examService.syncExamsForClass(classId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Synced successfully")
                        .message("Created " + created + " new exam(s)")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
