package com.java.slms.controller;

import com.java.slms.dto.ClassInfoResponse;
import com.java.slms.dto.ClassRequestDto;
import com.java.slms.dto.ClassResponseDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.ClassEntityService;
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
@RequiredArgsConstructor
@RequestMapping("/api/classes")
@Tag(name = "Class Entity Controller", description = "Manage classes and associated fee structures")
public class ClassEntityController
{

    private final ClassEntityService classEntityService;

    @Operation(
            summary = "Add a new class",
            description = "Creates a new class along with its fee structure for an active session.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Class created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or class already exists", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<ClassResponseDto>> addClass(@RequestBody ClassRequestDto classRequestDto)
    {
        ClassResponseDto createdClass = classEntityService.addClass(classRequestDto);
        RestResponse<ClassResponseDto> response = RestResponse.<ClassResponseDto>builder()
                .data(createdClass)
                .message("Class created with FeeStructure successfully")
                .status(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all classes in the active session",
            description = "Retrieves all classes along with fee and student details for the active session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Classes fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<ClassInfoResponse>>> getAllClasses()
    {
        List<ClassInfoResponse> classes = classEntityService.getAllClassInActiveSession();
        RestResponse<List<ClassInfoResponse>> response = RestResponse.<List<ClassInfoResponse>>builder()
                .data(classes)
                .message("Total Classes - " + classes.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get class details by class and session ID",
            description = "Fetches a class with fee and student information by classId and sessionId.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true),
                    @Parameter(name = "sessionId", description = "ID of the session", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Class fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or Class or fee data not found", content = @Content),
            }
    )
    @GetMapping("/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<ClassInfoResponse>> getClassByName(@PathVariable Long classId, @PathVariable Long sessionId)
    {
        ClassInfoResponse classDto = classEntityService.getClassByClassIdAndSessionId(classId, sessionId);
        RestResponse<ClassInfoResponse> response = RestResponse.<ClassInfoResponse>builder()
                .data(classDto)
                .message("Class fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update class name",
            description = "Updates the class name and fee structure for the specified class ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the class to update", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Class name updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or inactive session or Class not fount", content = @Content),
            }
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<ClassInfoResponse>> updateClassName(
            @PathVariable Long id,
            @RequestBody ClassRequestDto classRequestDto
    )
    {
        ClassInfoResponse updatedClass = classEntityService.updateClassNameById(id, classRequestDto);
        RestResponse<ClassInfoResponse> response = RestResponse.<ClassInfoResponse>builder()
                .data(updatedClass)
                .message("Class name updated successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete class by class and session ID",
            description = "Deletes the class for the given classId and sessionId if the session is active.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class to delete", required = true),
                    @Parameter(name = "sessionId", description = "ID of the session", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Class deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or inactive session or Class not fount", content = @Content),
            }
    )
    @DeleteMapping("/{classId}/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<String>> deleteClass(@PathVariable Long classId, @PathVariable Long sessionId)
    {
        classEntityService.deleteClassByIdAndSessionId(classId, sessionId);
        RestResponse<String> response = RestResponse.<String>builder()
                .data(null)
                .message("Class deleted successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }
}
