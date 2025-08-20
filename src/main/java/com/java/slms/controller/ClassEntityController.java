package com.java.slms.controller;

import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.ClassEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class ClassEntityController
{
    private final ClassEntityService classEntityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponseDto>> addClass(@RequestBody ClassRequestDto classRequestDto)
    {
        ClassResponseDto createdClass = classEntityService.addClass(classRequestDto);
        ApiResponse<ClassResponseDto> response = ApiResponse.<ClassResponseDto>builder()
                .data(createdClass)
                .message("Class created with FeeStructure successfully")
                .status(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ClassResponseDto>>> getAllClasses()
    {
        List<ClassResponseDto> classes = classEntityService.getAllClass();
        ApiResponse<List<ClassResponseDto>> response = ApiResponse.<List<ClassResponseDto>>builder()
                .data(classes)
                .message("Total Classes - " + classes.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/with-sections")
//    public ResponseEntity<ApiResponse<List<ClassEntityDto>>> getClassesWithSections() {
//        List<ClassEntityDto> classList = classEntityService.getClassWithSections();
//        ApiResponse<List<ClassEntityDto>> response = ApiResponse.<List<ClassEntityDto>>builder()
//                .data(classList)
//                .message("Classes with all sections retrieved successfully")
//                .status(HttpStatus.OK.value())
//                .build();
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponseDto>> getClassByName(@PathVariable Long classId, @PathVariable Long sessionId)
    {
        ClassResponseDto classDto = classEntityService.getClassByClassIdAndSessionId(classId, sessionId);
        ApiResponse<ClassResponseDto> response = ApiResponse.<ClassResponseDto>builder()
                .data(classDto)
                .message("Class fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponseDto>> updateClassName(
            @PathVariable Long id,
            @RequestBody ClassRequestDto classRequestDto
    )
    {
        ClassResponseDto updatedClass = classEntityService.updateClassNameById(id, classRequestDto);
        ApiResponse<ClassResponseDto> response = ApiResponse.<ClassResponseDto>builder()
                .data(updatedClass)
                .message("Class name updated successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteClass(@PathVariable Long classId, @PathVariable Long sessionId)
    {
        classEntityService.deleteClassByIdAndSessionId(classId, sessionId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data(null)
                .message("Class deleted successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }
}
