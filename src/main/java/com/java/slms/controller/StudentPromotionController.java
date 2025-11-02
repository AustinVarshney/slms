package com.java.slms.controller;

import com.java.slms.dto.PromotionAssignmentRequest;
import com.java.slms.dto.StudentPromotionDto;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.StudentPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Promotion Controller", description = "APIs for managing student promotions")
public class StudentPromotionController {

    private final StudentPromotionService promotionService;
    private final TeacherRepository teacherRepository;

    @Operation(
            summary = "Assign promotion for a student (Class Teacher)",
            description = "Class teacher assigns promotion/graduation for a student",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotion assigned successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<StudentPromotionDto>> assignPromotion(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestBody PromotionAssignmentRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherRepository.findByEmailIgnoreCaseAndSchoolId(email, schoolId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        log.info("Teacher {} assigning promotion for student {}", email, request.getStudentPan());

        StudentPromotionDto promotion = promotionService.assignPromotion(teacher, request, schoolId);

        return ResponseEntity.ok(
                RestResponse.<StudentPromotionDto>builder()
                        .data(promotion)
                        .message("Promotion assigned successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get promotions for a class (Class Teacher)",
            description = "Get all promotion assignments for a specific class",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<StudentPromotionDto>>> getPromotionsByClass(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long classId,
            @RequestParam Long sessionId
    ) {
        log.info("Fetching promotions for class: {}", classId);

        List<StudentPromotionDto> promotions = promotionService.getPromotionsByClass(classId, sessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<StudentPromotionDto>>builder()
                        .data(promotions)
                        .message("Promotions retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get promotion for a student",
            description = "Get promotion assignment for a specific student",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotion retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content)
            }
    )
    @GetMapping("/student/{studentPan}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<StudentPromotionDto>> getPromotionByStudent(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable String studentPan,
            @RequestParam Long sessionId
    ) {
        log.info("Fetching promotion for student: {}", studentPan);

        StudentPromotionDto promotion = promotionService.getPromotionByStudent(studentPan, sessionId, schoolId);

        if (promotion == null) {
            return ResponseEntity.ok(
                    RestResponse.<StudentPromotionDto>builder()
                            .data(null)
                            .message("No promotion assigned yet")
                            .status(HttpStatus.OK.value())
                            .build()
            );
        }

        return ResponseEntity.ok(
                RestResponse.<StudentPromotionDto>builder()
                        .data(promotion)
                        .message("Promotion retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Execute promotions (Admin only)",
            description = "Execute all pending promotions for session change",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotions executed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PostMapping("/execute")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> executePromotions(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestParam Long fromSessionId,
            @RequestParam Long toSessionId
    ) {
        log.info("Executing promotions from session {} to {}", fromSessionId, toSessionId);

        promotionService.executePromotions(fromSessionId, toSessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .data(null)
                        .message("Promotions executed successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete a promotion assignment (Class Teacher)",
            description = "Delete a promotion assignment that hasn't been executed yet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotion deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<Void>> deletePromotion(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long promotionId
    ) {
        log.info("Deleting promotion: {}", promotionId);

        promotionService.deletePromotion(promotionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .data(null)
                        .message("Promotion deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get pending promotions (Admin)",
            description = "Get all pending promotions for a session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pending promotions retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<StudentPromotionDto>>> getPendingPromotions(
            @RequestAttribute("schoolId") Long schoolId,
            @RequestParam Long sessionId
    ) {
        log.info("Fetching pending promotions for session: {}", sessionId);

        List<StudentPromotionDto> promotions = promotionService.getPendingPromotions(sessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<StudentPromotionDto>>builder()
                        .data(promotions)
                        .message("Pending promotions retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all promotions for a session (Admin)",
            description = "Get all promotions for a session regardless of status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<StudentPromotionDto>>> getPromotionsBySession(
            @RequestAttribute("schoolId") Long schoolId,
            @PathVariable Long sessionId
    ) {
        log.info("Fetching all promotions for session: {}", sessionId);

        List<StudentPromotionDto> promotions = promotionService.getPromotionsBySession(sessionId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<StudentPromotionDto>>builder()
                        .data(promotions)
                        .message("Promotions retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
