package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import com.java.slms.service.TeacherService;
import com.java.slms.service.TransferCertificateRequestService;
import com.java.slms.util.RequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/tc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transfer Certificate Requests", description = "APIs to manage transfer certificate requests")
public class TransferRequestController
{

    private final TransferCertificateRequestService transferCertificateService;
    private final AdminService adminService;
    private final TeacherService teacherService;

    @Operation(summary = "Submit a transfer certificate request",
            description = "Allows a student to submit a transfer certificate request.",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Request submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or already existing request", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/request")
    public ResponseEntity<RestResponse<TransferCertificateRequestDto>> createTransferCertificateRequest(
            @RequestBody TCReasonDto reasonDto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        String studentPan = SecurityContextHolder.getContext().getAuthentication().getName();

        TransferCertificateRequestDto createdRequest =
                transferCertificateService.createTransferCertificateRequest(studentPan, reasonDto, schoolId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<TransferCertificateRequestDto>builder()
                        .data(createdRequest)
                        .message("Transfer Certificate request submitted successfully.")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(summary = "Get current student's transfer certificate requests",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/requests/me")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsByCurrentStudent(
            @RequestAttribute("schoolId") Long schoolId)
    {

        String studentPan = SecurityContextHolder.getContext().getAuthentication().getName();

        List<TransferCertificateRequestDto> requests =
                transferCertificateService.getAllRequestsByStudentPan(studentPan, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get transfer certificate requests by student PAN",
            parameters = {
                    @Parameter(name = "studentPan", description = "PAN number of the student", required = true),
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @GetMapping("/requests/{studentPan}")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsByStudentPan(
            @PathVariable String studentPan,
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequestsByStudentPan(studentPan, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Process a transfer certificate request",
            parameters = {
                    @Parameter(name = "requestId", description = "ID of the transfer certificate request", required = true),
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request processed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid decision or invalid request", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/process/{requestId}")
    public ResponseEntity<RestResponse<TransferCertificateRequestDto>> processRequest(
            @PathVariable Long requestId,
            @RequestBody ProcessRequestDto processRequestDto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        TransferCertificateRequestDto responseDto = transferCertificateService.processRequestDecision(
                requestId,
                processRequestDto.getAdminReply(),
                processRequestDto.getDecision(),
                schoolId
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

    @Operation(summary = "Get all transfer certificate requests",
            parameters = {
                    @Parameter(name = "status", description = "Filter requests by status (optional)"),
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Requests fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequests(status, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Forward TC request to class teacher",
            parameters = {
                    @Parameter(name = "tcRequestId", description = "ID of the TC request", required = true),
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Message from admin to class teacher",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminToTeacherDto.class)
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "TC request successfully forwarded to class teacher"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            })
    @PutMapping("/{tcRequestId}/forward-to-teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> forwardTCRequestToTeacher(
            @PathVariable Long tcRequestId,
            @RequestBody AdminToTeacherDto adminToTeacherDto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminService.getAdminInfo(email, schoolId);

        transferCertificateService.forwardTCRequestToClassTeacher(tcRequestId, admin, adminToTeacherDto, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("TC request forwarded to class teacher successfully")
                        .build()
        );
    }

    @Operation(summary = "Reply to a transfer certificate request from teacher to admin",
            parameters = {
                    @Parameter(name = "tcRequestId", description = "ID of the TC request", required = true),
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Response from class teacher with message and decision",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeacherToAdminDto.class)
                    )),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Response submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            })
    @PutMapping("/{tcRequestId}/reply-to-admin")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<Void>> replyToTCRequestFromTeacher(
            @PathVariable Long tcRequestId,
            @RequestBody TeacherToAdminDto teacherToAdminDto,
            @RequestAttribute("schoolId") Long schoolId)
    {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherService.getActiveTeacherByEmail(email, schoolId);

        transferCertificateService.replyTCRequestToAdmin(tcRequestId, teacher, teacherToAdminDto, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Teacher response submitted successfully.")
                        .build()
        );
    }

    @Operation(summary = "Get all TC requests forwarded to class teacher",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List fetched successfully"),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/forwarded-to-class-teacher")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getAllRequestsForwardedToClassTeacher(
            @RequestAttribute("schoolId") Long schoolId)
    {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherService.getActiveTeacherByEmail(email, schoolId);

        List<TransferCertificateRequestDto> forwardedRequests =
                transferCertificateService.getAllRequestForwardedByAdminToClassTeacher(teacher, schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Forwarded TC requests fetched successfully.")
                        .data(forwardedRequests)
                        .build()
        );
    }

    @Operation(summary = "Get all processed TC requests from last 1 month",
            description = "Fetches all TC requests that were approved or rejected by admin in the last month",
            parameters = {
                    @Parameter(name = "schoolId", description = "School ID from request attribute", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Processed requests fetched successfully"),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content)
            })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/processed/last-month")
    public ResponseEntity<RestResponse<List<TransferCertificateRequestDto>>> getProcessedRequestsFromLastMonth(
            @RequestAttribute("schoolId") Long schoolId)
    {
        List<TransferCertificateRequestDto> processedRequests = 
                transferCertificateService.getProcessedRequestsFromLastMonth(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<TransferCertificateRequestDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Processed TC requests from last month fetched successfully. Total: " + processedRequests.size())
                        .data(processedRequests)
                        .build()
        );
    }

}
