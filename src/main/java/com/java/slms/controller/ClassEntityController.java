package com.java.slms.controller;

import com.java.slms.dto.ClassInfoResponse;
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
    public ResponseEntity<ApiResponse<List<ClassInfoResponse>>> getAllClasses()
    {
        List<ClassInfoResponse> classes = classEntityService.getAllClassInActiveSession();
        ApiResponse<List<ClassInfoResponse>> response = ApiResponse.<List<ClassInfoResponse>>builder()
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
    public ResponseEntity<ApiResponse<ClassInfoResponse>> getClassByName(@PathVariable Long classId, @PathVariable Long sessionId)
    {
        ClassInfoResponse classDto = classEntityService.getClassByClassIdAndSessionId(classId, sessionId);
        ApiResponse<ClassInfoResponse> response = ApiResponse.<ClassInfoResponse>builder()
                .data(classDto)
                .message("Class fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ClassInfoResponse>> updateClassName(
            @PathVariable Long id,
            @RequestBody ClassRequestDto classRequestDto
    )
    {
        ClassInfoResponse updatedClass = classEntityService.updateClassNameById(id, classRequestDto);
        ApiResponse<ClassInfoResponse> response = ApiResponse.<ClassInfoResponse>builder()
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
