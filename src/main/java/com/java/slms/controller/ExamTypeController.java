package com.java.slms.controller;


import com.java.slms.dto.ExamTypeRequestDto;
import com.java.slms.dto.ExamTypeResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.ExamTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exam Type Controller", description = "APIs for managing exam types")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ExamTypeController
{

    private final ExamTypeService examTypeService;

    @Operation(summary = "Create a new Exam Type")
    @PostMapping
    public ResponseEntity<RestResponse<ExamTypeResponseDto>> createExamType(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestBody ExamTypeRequestDto dto)
    {
        log.info("Received request to create exam type for schoolId={}", schoolId);
        ExamTypeResponseDto created = examTypeService.createExamType(schoolId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.<ExamTypeResponseDto>builder()
                        .data(created)
                        .message("Exam Type created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build());
    }

    @Operation(summary = "Get Exam Type by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ExamTypeResponseDto>> getExamTypeById(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {
        log.info("Received request to get exam type with id: {} for schoolId={}", id, schoolId);
        ExamTypeResponseDto dto = examTypeService.getExamTypeById(schoolId, id);

        return ResponseEntity.ok(RestResponse.<ExamTypeResponseDto>builder()
                .data(dto)
                .message("Exam Type fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Get all Exam Types")
    @GetMapping
    public ResponseEntity<RestResponse<List<ExamTypeResponseDto>>> getAllExamTypes(
            @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Received request to get all exam types for schoolId={}", schoolId);
        List<ExamTypeResponseDto> list = examTypeService.getAllExamTypes(schoolId);

        return ResponseEntity.ok(RestResponse.<List<ExamTypeResponseDto>>builder()
                .data(list)
                .message("All Exam Types fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Update Exam Type by ID")
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ExamTypeResponseDto>> updateExamType(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody ExamTypeRequestDto dto)
    {
        log.info("Received request to update exam type with id: {} for schoolId={}", id, schoolId);
        ExamTypeResponseDto updated = examTypeService.updateExamType(schoolId, id, dto);

        return ResponseEntity.ok(RestResponse.<ExamTypeResponseDto>builder()
                .data(updated)
                .message("Exam Type updated successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(summary = "Delete Exam Type by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<String>> deleteExamType(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long id)
    {
        log.info("Received request to delete exam type with id: {} for schoolId={}", id, schoolId);
        examTypeService.deleteExamType(schoolId, id);

        return ResponseEntity.ok(RestResponse.<String>builder()
                .data("Deleted successfully")
                .message("Exam Type deleted successfully")
                .status(HttpStatus.OK.value())
                .build());
    }
}
