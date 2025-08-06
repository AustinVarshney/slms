package com.java.slms.controller;

import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController
{

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectDto>> addSubject(@RequestBody SubjectDto subjectDto)
    {
        SubjectDto created = subjectService.addSubject(subjectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SubjectDto>builder()
                        .data(created)
                        .message("Subject created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> addSubjectsByClass(@RequestBody SubjectsBulkDto bulkDto)
    {
        List<SubjectDto> created = subjectService.addSubjectsByClass(bulkDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<List<SubjectDto>>builder()
                        .data(created)
                        .message("Subjects created for class: " + bulkDto.getClassId())
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getAllSubjects()
    {
        List<SubjectDto> list = subjectService.getAllSubjects();
        return ResponseEntity.ok(
                ApiResponse.<List<SubjectDto>>builder()
                        .data(list)
                        .message("Total subjects: " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectDto>> getSubjectByName(@PathVariable Long id)
    {
        SubjectDto dto = subjectService.getSubjectById(id);
        return ResponseEntity.ok(
                ApiResponse.<SubjectDto>builder()
                        .data(dto)
                        .message("Subject found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getSubjectsByClassId(@PathVariable Long classId)
    {
        List<SubjectDto> subjectDtos = subjectService.getSubjectsByClassId(classId);

        return ResponseEntity.ok(
                ApiResponse.<List<SubjectDto>>builder()
                        .data(subjectDtos)
                        .message("Subjects for class ID " + classId + ": " + subjectDtos.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PatchMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectDto>> updateSubject(@PathVariable Long subjectId, @RequestBody SubjectDto subjectDto)
    {
        SubjectDto updated = subjectService.updateSubjectById(subjectId, subjectDto);
        return ResponseEntity.ok(
                ApiResponse.<SubjectDto>builder()
                        .data(updated)
                        .message("Subject updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/subject/{subjectId}/class/{classId}")
    public ResponseEntity<ApiResponse<String>> deleteSubject(@PathVariable Long subjectId, @PathVariable Long classId)
    {
        subjectService.deleteSubject(subjectId, classId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .data(null)
                        .message("Subject deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
