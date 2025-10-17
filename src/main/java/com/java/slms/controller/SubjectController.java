package com.java.slms.controller;

import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
@Tag(name = "Subject Controller", description = "APIs for managing subjects")
public class SubjectController
{

    private final SubjectService subjectService;

    @Operation(
            summary = "Add a new subject",
            description = "Creates a new subject for a class in the given school.",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Subject created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or subject already exists", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<RestResponse<SubjectDto>> addSubject(
            @RequestBody SubjectDto subjectDto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        SubjectDto created = subjectService.addSubject(subjectDto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<SubjectDto>builder()
                        .data(created)
                        .message("Subject created")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Add multiple subjects by class",
            description = "Creates multiple subjects in bulk for a specific class in the given school.",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Subjects created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping("/multiple")
    public ResponseEntity<RestResponse<List<SubjectDto>>> addSubjectsByClass(
            @RequestBody SubjectsBulkDto bulkDto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<SubjectDto> created = subjectService.addSubjectsByClass(bulkDto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<List<SubjectDto>>builder()
                        .data(created)
                        .message("Subjects created for class: " + bulkDto.getClassId())
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all subjects",
            description = "Fetches all subjects across classes in a given school.",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subjects retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<SubjectDto>>> getAllSubjects(
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<SubjectDto> list = subjectService.getAllSubjects(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<SubjectDto>>builder()
                        .data(list)
                        .message("Total subjects: " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get subject by ID",
            description = "Fetches a particular subject by its ID and school ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the subject", required = true),
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subject found successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid subject ID or subject not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<RestResponse<SubjectDto>> getSubjectById(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId)
    {
        SubjectDto dto = subjectService.getSubjectById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<SubjectDto>builder()
                        .data(dto)
                        .message("Subject found")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get subjects by class ID",
            description = "Fetches all subjects related to a specific class in the given school.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true),
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subjects fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid class ID or no subjects found", content = @Content)
            }
    )
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<SubjectDto>>> getSubjectsByClassId(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<SubjectDto> subjectDtos = subjectService.getSubjectsByClassId(classId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<SubjectDto>>builder()
                        .data(subjectDtos)
                        .message("Subjects for class ID " + classId + ": " + subjectDtos.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update subject information",
            description = "Updates subject details by subject ID in the given school.",
            parameters = {
                    @Parameter(name = "subjectId", description = "ID of the subject", required = true),
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subject updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or subject not found", content = @Content)
            }
    )
    @PutMapping("/{subjectId}")
    public ResponseEntity<RestResponse<SubjectDto>> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectDto subjectDto,
            @RequestAttribute("schoolId") Long schoolId)
    {
        SubjectDto updated = subjectService.updateSubjectInfoById(subjectId, subjectDto, schoolId);
        return ResponseEntity.ok(
                RestResponse.<SubjectDto>builder()
                        .data(updated)
                        .message("Subject updated")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete subject",
            description = "Deletes a subject by ID and class ID in the given school.",
            parameters = {
                    @Parameter(name = "subjectId", description = "ID of the subject", required = true),
                    @Parameter(name = "classId", description = "ID of the class", required = true),
                    @Parameter(name = "schoolId", description = "School ID (injected from request)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subject deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or subject not found", content = @Content)
            }
    )
    @DeleteMapping("/subject/{subjectId}/class/{classId}")
    public ResponseEntity<RestResponse<String>> deleteSubject(
            @PathVariable Long subjectId,
            @RequestAttribute("schoolId") Long schoolId)
    {
        subjectService.deleteSubject(subjectId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .data(null)
                        .message("Subject deleted")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
