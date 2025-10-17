package com.java.slms.controller;

import com.java.slms.dto.ClassExamBulkRequestDto;
import com.java.slms.dto.ClassExamResponseDto;
import com.java.slms.dto.ClassExamUpdateDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.ClassExamService;
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
@RequestMapping("/api/class-exams")
@RequiredArgsConstructor
@Tag(name = "Class Exam Management")
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ClassExamController
{

    private final ClassExamService classExamService;

    @Operation(
            summary = "Assign ExamType to multiple classes",
            description = "Assigns one exam type to multiple classes with individual max & passing marks"
    )
    @PostMapping("/assign")
    public ResponseEntity<RestResponse<String>> assignExamToClasses(
            @RequestBody ClassExamBulkRequestDto dto,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        classExamService.assignExamTypeToMultipleClasses(dto, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("ExamType assigned successfully to selected classes.")
                        .message("Bulk assignment completed")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "List exams assigned to a class")
    @GetMapping("/class/{classId}")
    public ResponseEntity<RestResponse<List<ClassExamResponseDto>>> getByClass(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        var data = classExamService.getExamsByClass(classId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<ClassExamResponseDto>>builder()
                        .data(data)
                        .message("Found: " + data.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Update a class exam assignment")
    @PutMapping("/{classId}/{examTypeId}")
    public ResponseEntity<RestResponse<String>> updateClassExam(
            @PathVariable Long classId,
            @PathVariable Long examTypeId,
            @RequestBody ClassExamUpdateDto dto,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        classExamService.updateClassExam(classId, examTypeId, dto, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Updated successfully")
                        .message("ClassExam updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Delete (unassign) exam from class")
    @DeleteMapping("/{classId}/{examTypeId}")
    public ResponseEntity<RestResponse<String>> deleteClassExam(
            @PathVariable Long classId,
            @PathVariable Long examTypeId,
            @RequestAttribute("schoolId") Long schoolId
    )
    {
        classExamService.deleteClassExam(classId, examTypeId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data("Deleted successfully")
                        .message("ClassExam removed")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
