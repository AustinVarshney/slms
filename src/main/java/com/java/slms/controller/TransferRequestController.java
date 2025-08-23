package com.java.slms.controller;

import com.java.slms.dto.ProcessRequestDto;
import com.java.slms.dto.TransferCertificateRequestDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.TransferCertificateRequestService;
import com.java.slms.util.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tc")
@RequiredArgsConstructor
public class TransferRequestController
{
    private final TransferCertificateRequestService transferCertificateService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<TransferCertificateRequestDto>> createTransferCertificateRequest(
            @RequestBody TransferCertificateRequestDto requestDto)
    {

        String studentPan = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TransferCertificateRequestDto createdRequest = transferCertificateService.createTransferCertificateRequest(studentPan, requestDto);

        return ResponseEntity.ok(
                ApiResponse.<TransferCertificateRequestDto>builder()
                        .data(createdRequest)
                        .message("Transfer Certificate request submitted successfully.")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/requests/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<TransferCertificateRequestDto>>> getAllRequestsByCurrentStudent()
    {

        String studentPan = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequestsByStudentPan(studentPan);

        return ResponseEntity.ok(
                ApiResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


    @GetMapping("/requests/{studentPan}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferCertificateRequestDto>>> getAllRequestsByStudentPan(
            @PathVariable String studentPan)
    {

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequestsByStudentPan(studentPan);

        return ResponseEntity.ok(
                ApiResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message("Transfer Certificate requests fetched successfully. Total requests: " + requests.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/process/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TransferCertificateRequestDto>> processRequest(
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

        return ResponseEntity.ok(ApiResponse.<TransferCertificateRequestDto>builder()
                .data(responseDto)
                .message(message)
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferCertificateRequestDto>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status)
    {

        List<TransferCertificateRequestDto> requests = transferCertificateService.getAllRequests(status);

        String message = "Transfer Certificate requests fetched successfully. Total requests: " + requests.size();

        return ResponseEntity.ok(
                ApiResponse.<List<TransferCertificateRequestDto>>builder()
                        .data(requests)
                        .message(message)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


}
