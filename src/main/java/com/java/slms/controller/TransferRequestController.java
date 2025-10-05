package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import com.java.slms.service.TeacherService;
import com.java.slms.service.TransferCertificateRequestService;
import com.java.slms.util.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transfer Certificate Requests", description = "APIs to manage transfer certificate requests")
public class TransferRequestController
{
    private final TransferCertificateRequestService transferCertificateService;
    private final AdminService adminService;
    private final TeacherService teacherService;

    @Operation(
            summary = "Submit a transfer certificate request",
            description = "Allows a student to submit a transfer certificate request.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request submitted successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or already existing request", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/request")
    public ResponseEntity<RestResponse<TransferCertificateRequestDto>> createTransferCertificateRequest(
            @RequestBody TCReasonDto reasonDto)
    {
        String studentPan = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TransferCertificateRequestDto createdRequest = transferCertificateService.createTransferCertificateRequest(studentPan, reasonDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<TransferCertificateRequestDto>builder()
                        .data(createdRequest)
                        .message("Transfer Certificate request submitted successfully.")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get current student's transfer certificate requests",
            description = "Fetch all transfer certificate requests submitted by the logged-in student.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/requests/me")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsByCurrentStudent()
    {
        String studentPan = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequestsByStudentPan(studentPan);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get transfer certificate requests by student PAN",
            description = "Allows admin to fetch all transfer certificate requests for a specific student.",
            parameters = {
                    @Parameter(name = "studentPan", description = "PAN number of the student", required = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @GetMapping("/requests/{studentPan}")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsByStudentPan(
            @PathVariable String studentPan)
    {
        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequestsByStudentPan(studentPan);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Process a transfer certificate request",
            description = "Allows admin to approve or reject a transfer certificate request.",
            parameters = {
                    @Parameter(name = "requestId", description = "ID of the transfer certificate request", required = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid decision or invalid request", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/process/{requestId}")
    public ResponseEntity<RestResponse<TransferCertificateRequestDto>> processRequest(
            @PathVariable Long requestId,
            @RequestBody ProcessRequestDto processRequestDto)
    {
        TransferCertificateRequestDto responseDto = transferCertificateService.processRequestDecision(
                requestId,
                processRequestDto.getAdminReply(),
                processRequestDto.getDecision()
        );

        String message = responseDto.getStatus() == RequestStatus.APPROVED ?
                "Transfer Certificate request approved successfully." :
                "Transfer Certificate request rejected successfully.";

        return ResponseEntity.ok(RestResponse.<TransferCertificateRequestDto>builder()
                .data(responseDto)
                .message(message)
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get all transfer certificate requests",
            description = "Allows admin to fetch all transfer certificate requests, optionally filtered by status.",
            parameters = {
                    @Parameter(name = "status", description = "Filter requests by status (optional)")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status)
    {
        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequests(status);

        String message = "Transfer Certificate requests fetched successfully. Total requests: " + requests.size();

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message(message)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Forward TC request to class teacher",
            description = "Allows admin to forward a transfer certificate request to the respective class teacher.",
            parameters = {
                    @Parameter(name = "tcRequestId", description = "ID of the TC request", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Message from admin to class teacher",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminToTeacherDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "TC request successfully forwarded to class teacher"),
                    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., already approved or not found)", content = @Content)
            }
    )
    @PutMapping("/{tcRequestId}/forward-to-teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> forwardTCRequestToTeacher(
            @PathVariable Long tcRequestId,
            @RequestBody AdminToTeacherDto adminToTeacherDto)
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Admin admin = adminService.getActiveAdminByEmail(email);

        log.info("Forwarding TC request ID {} to class teacher", tcRequestId);

        transferCertificateService.forwardTCRequestToClassTeacher(tcRequestId, admin, adminToTeacherDto);

        log.info("Successfully forwarded TC request ID {}", tcRequestId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("TC request forwarded to class teacher successfully")
                        .build()
        );
    }

    @Operation(
            summary = "Reply to a transfer certificate request from teacher to admin",
            description = "Allows a class teacher to respond (approve or reject) to a TC request forwarded by the admin.",
            parameters = {
                    @Parameter(name = "tcRequestId", description = "ID of the TC request", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Response from class teacher with message and decision",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeacherToAdminDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Response submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., not assigned to teacher or already responded)", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PutMapping("/{tcRequestId}/reply-to-admin")
    public ResponseEntity<RestResponse<Void>> replyToTCRequestFromTeacher(
            @PathVariable Long tcRequestId,
            @RequestBody TeacherToAdminDto teacherToAdminDto)
    {

        log.info("Teacher replying to TC request ID {} with decision: {}", tcRequestId, teacherToAdminDto.getTeacherReplyToAdmin());

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Teacher teacher = teacherService.getActiveTeacherByEmail(email);

        transferCertificateService.replyTCRequestToAdmin(tcRequestId, teacher, teacherToAdminDto);

        log.info("Reply submitted successfully for TC request ID {}", tcRequestId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Teacher response submitted successfully.")
                        .build()
        );
    }

    @Operation(
            summary = "Get all TC requests forwarded to class teacher",
            description = "Returns a list of all transfer certificate requests that have been forwarded by the admin to the currently logged-in class teacher.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List fetched successfully"),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/forwarded-to-class-teacher")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsForwardedToClassTeacher()
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Teacher teacher = teacherService.getActiveTeacherByEmail(email);

        List<TransferCertificateRequestDto> forwardedRequests =
                transferCertificateService.getAllRequestForwardedByAdminToClassTeacher(teacher);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Forwarded TC requests fetched successfully.")
                        .data(forwardedRequests)
                        .build()
        );
    }


}
