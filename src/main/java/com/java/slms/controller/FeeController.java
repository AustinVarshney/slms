package com.java.slms.controller;

import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.FeeResponseDTO;
import com.java.slms.dto.StudentDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.FeeService;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Slf4j
public class FeeController
{

    private final FeeService feeService;

    // ---------------- Create Fee ----------------
    @PostMapping
    public ResponseEntity<ApiResponse<FeeResponseDTO>> createFee(@RequestBody FeeRequestDTO dto)
    {
        FeeResponseDTO saved = feeService.createFee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FeeResponseDTO>builder()
                        .data(saved)
                        .message("Fee created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    // ---------------- Get Fees by Student PAN ----------------
    @GetMapping("/student/{panNumber}")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getFeesByStudentPan(
            @PathVariable String panNumber,
            @RequestParam(required = false) FeeMonth month)
    {

        List<FeeResponseDTO> fees = (month != null)
                ? feeService.getFeesByStudentPan(panNumber, month)
                : feeService.getFeesByStudentPan(panNumber);

        return ResponseEntity.ok(ApiResponse.<List<FeeResponseDTO>>builder()
                .data(fees)
                .message("Fees fetched for student")
                .status(HttpStatus.OK.value())
                .build());
    }

    // ---------------- Get Fees by FeeStructure ----------------
    @GetMapping("/structure/{feeStructureId}")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getFeesByFeeStructure(
            @PathVariable Long feeStructureId,
            @RequestParam(required = false) FeeMonth month)
    {

        List<FeeResponseDTO> fees = (month != null)
                ? feeService.getFeesByFeeStructureId(feeStructureId, month)
                : feeService.getFeesByFeeStructureId(feeStructureId);

        return ResponseEntity.ok(ApiResponse.<List<FeeResponseDTO>>builder()
                .data(fees)
                .message("Fees fetched for FeeStructure")
                .status(HttpStatus.OK.value())
                .build());
    }

    // ---------------- Get Fees by Status ----------------
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getFeesByStatus(
            @PathVariable FeeStatus status,
            @RequestParam(required = false) FeeMonth month)
    {

        List<FeeResponseDTO> fees = (month != null)
                ? feeService.getFeesByStatus(status, month)
                : feeService.getFeesByStatus(status);

        return ResponseEntity.ok(ApiResponse.<List<FeeResponseDTO>>builder()
                .data(fees)
                .message("Fees fetched by status")
                .status(HttpStatus.OK.value())
                .build());
    }

    // ---------------- Get Fees by Paid Date Range ----------------
    @GetMapping("/paid")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getFeesPaidBetween(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate)
    {

        List<FeeResponseDTO> fees = feeService.getFeesPaidBetween(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<List<FeeResponseDTO>>builder()
                .data(fees)
                .message("Fees fetched between dates")
                .status(HttpStatus.OK.value())
                .build());
    }

    // ---------------- Get Fees by Student PAN & Status ----------------
    @GetMapping("/student/{panNumber}/status/{status}")
    public ResponseEntity<ApiResponse<List<FeeResponseDTO>>> getFeesByStudentPanAndStatus(
            @PathVariable String panNumber,
            @PathVariable FeeStatus status,
            @RequestParam(required = false) FeeMonth month)
    {

        List<FeeResponseDTO> fees = (month != null)
                ? feeService.getFeesByStudentPanAndStatus(panNumber, status, month)
                : feeService.getFeesByStudentPanAndStatus(panNumber, status);

        return ResponseEntity.ok(ApiResponse.<List<FeeResponseDTO>>builder()
                .data(fees)
                .message("Fees fetched for student by status")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/defaulters/{feeStructureId}/{month}")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getDefaulters(
            @PathVariable Long feeStructureId,
            @PathVariable FeeMonth month)
    {

        List<StudentDto> defaulters = feeService.getDefaulters(feeStructureId, month);

        return ResponseEntity.ok(
                ApiResponse.<List<StudentDto>>builder()
                        .data(defaulters)
                        .message("Defaulter list fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
