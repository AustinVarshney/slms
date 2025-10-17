package com.java.slms.controller;

import com.java.slms.dto.CreateMarksDto;
import com.java.slms.dto.StudentExamSummaryDto;
import com.java.slms.dto.StudentMarksResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentTermMarksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-marks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Term Marks", description = "Manage marks of students for various exams")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class StudentTermMarksController
{

    private final StudentTermMarksService studentTermMarksService;

    @Operation(summary = "Add marks of students for a given class and exam type (in current session)")
    @PostMapping("/class/{classId}/subject/{subjectId}")
    public ResponseEntity<RestResponse<String>> addStudentMarks(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @RequestBody CreateMarksDto marksDto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Adding student marks for classId={}, subjectId={}, schoolId={}", classId, subjectId, schoolId);
        studentTermMarksService.addMarksOfStudentsByExamTypeAndClassIdInCurrentSession(marksDto, schoolId, subjectId, classId);

        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Marks added successfully")
                        .message("Student marks recorded")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/students/{panNumber}/marks")
    public ResponseEntity<RestResponse<StudentMarksResponseDto>> getStudentMarks(@PathVariable String panNumber)
    {
        StudentMarksResponseDto response = studentTermMarksService.getStudentMarks(panNumber);
        return ResponseEntity.ok(RestResponse.<StudentMarksResponseDto>builder()
                .data(response)
                .message("Student marks fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<StudentMarksResponseDto>> getCurrentStudentMarks()
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StudentMarksResponseDto response = studentTermMarksService.getStudentMarks(panNumber);
        return ResponseEntity.ok(RestResponse.<StudentMarksResponseDto>builder()
                .data(response)
                .message("Student marks fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/students/{panNumber}/exam-summary")
    public ResponseEntity<RestResponse<List<StudentExamSummaryDto>>> getExamSummary(
            @PathVariable String panNumber) {

        List<StudentExamSummaryDto> summaryList = studentTermMarksService.getExamSummaryByPanNumber(panNumber);

        return ResponseEntity.ok(
                RestResponse.<List<StudentExamSummaryDto>>builder()
                        .data(summaryList)
                        .message("Exam summary fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/exam-summary")
    public ResponseEntity<RestResponse<List<StudentExamSummaryDto>>> getExamSummaryOfCurrentStudent() {

        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<StudentExamSummaryDto> summaryList = studentTermMarksService.getExamSummaryByPanNumber(panNumber);

        return ResponseEntity.ok(
                RestResponse.<List<StudentExamSummaryDto>>builder()
                        .data(summaryList)
                        .message("Exam summary fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }




}