package com.java.slms.controller;

import com.java.slms.dto.FeeStructureRequestDTO;
import com.java.slms.dto.FeeStructureResponseDTO;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/fee-structures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class FeeStructureController
{

    private final FeeStructureService feeStructureService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeeStructureResponseDTO>> createFeeStructure(
            @RequestBody FeeStructureRequestDTO dto)
    {
        FeeStructureResponseDTO saved = feeStructureService.createFeeStructure(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FeeStructureResponseDTO>builder()
                        .data(saved)
                        .message("Fee Structure created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponseDTO>> updateFeeStructure(
            @PathVariable Long id,
            @RequestBody FeeStructureRequestDTO dto)
    {
        FeeStructureResponseDTO updated = feeStructureService.updateFeeStructure(id, dto);
        return ResponseEntity.ok(
                ApiResponse.<FeeStructureResponseDTO>builder()
                        .data(updated)
                        .message("Fee Structure updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponseDTO>> getFeeStructureById(@PathVariable Long id)
    {
        FeeStructureResponseDTO dto = feeStructureService.getFeeStructureById(id);
        return ResponseEntity.ok(
                ApiResponse.<FeeStructureResponseDTO>builder()
                        .data(dto)
                        .message("Fee Structure fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByClassId(@PathVariable Long classId)
    {
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByClassId(classId);
        return ResponseEntity.ok(
                ApiResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched for Class ID: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/type/{feeType}")
    public ResponseEntity<ApiResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByFeeType(@PathVariable String feeType)
    {
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByFeeType(feeType);
        return ResponseEntity.ok(
                ApiResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched for type: " + feeType)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/due-date-range")
    public ResponseEntity<ApiResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByDueDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate)
    {
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByDueDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched between due dates: " + startDate + " and " + endDate)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStructure(@PathVariable Long id)
    {
        feeStructureService.deleteFeeStructure(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Fee Structure deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
