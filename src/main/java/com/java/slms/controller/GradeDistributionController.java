package com.java.slms.controller;

import com.java.slms.dto.GradeDistributionDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.GradeDistributionService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/grade-distribution")
@RequiredArgsConstructor
@Tag(name = "Grade Distribution", description = "APIs for managing school grading systems")
@CrossOrigin(origins = "*")
public class GradeDistributionController
{
    private final GradeDistributionService gradeDistributionService;

    @Operation(
            summary = "Get grade distribution for school",
            description = "Retrieves the custom grade distribution for a school, or default if none is set",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Grade distribution retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<GradeDistributionDto>>> getGradeDistribution(
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<GradeDistributionDto> gradeDistribution = gradeDistributionService.getGradeDistribution(schoolId);
        
        RestResponse<List<GradeDistributionDto>> response = RestResponse.<List<GradeDistributionDto>>builder()
                .data(gradeDistribution)
                .message("Grade distribution retrieved successfully")
                .status(HttpStatus.OK.value())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Set custom grade distribution",
            description = "Sets a custom grade distribution for the school (Admin only)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Grade distribution set successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<GradeDistributionDto>>> setGradeDistribution(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestBody List<GradeDistributionDto> gradeDistributions)
    {
        List<GradeDistributionDto> savedDistribution = gradeDistributionService
                .setGradeDistribution(schoolId, gradeDistributions);
        
        RestResponse<List<GradeDistributionDto>> response = RestResponse.<List<GradeDistributionDto>>builder()
                .data(savedDistribution)
                .message("Grade distribution set successfully")
                .status(HttpStatus.OK.value())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Reset to default grade distribution",
            description = "Resets the school to use the default grading system (Admin only)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reset to default successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only", content = @Content)
            }
    )
    @DeleteMapping("/reset")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<GradeDistributionDto>>> resetToDefault(
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<GradeDistributionDto> defaultDistribution = gradeDistributionService.resetToDefault(schoolId);
        
        RestResponse<List<GradeDistributionDto>> response = RestResponse.<List<GradeDistributionDto>>builder()
                .data(defaultDistribution)
                .message("Reset to default grade distribution successfully")
                .status(HttpStatus.OK.value())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get default grade distribution",
            description = "Retrieves the system default grade distribution",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Default grade distribution retrieved")
            }
    )
    @GetMapping("/default")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<GradeDistributionDto>>> getDefaultGradeDistribution()
    {
        List<GradeDistributionDto> defaultDistribution = gradeDistributionService.getDefaultGradeDistribution();
        
        RestResponse<List<GradeDistributionDto>> response = RestResponse.<List<GradeDistributionDto>>builder()
                .data(defaultDistribution)
                .message("Default grade distribution retrieved successfully")
                .status(HttpStatus.OK.value())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
