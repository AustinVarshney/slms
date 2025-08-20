package com.java.slms.controller;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FEE_STAFF')")
public class FeeController
{
    private final FeeService feeService;

    @PutMapping("/pay")
    public ResponseEntity<ApiResponse<?>> payFees(@RequestBody FeeRequestDTO requestDto)
    {

        feeService.payFeesOfStudent(requestDto);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(null)
                        .message("Fees updated successfully.")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/catalogs")
    public ResponseEntity<ApiResponse<List<FeeCatalogDto>>> getAllFeeCatalogs()
    {
        List<FeeCatalogDto> feeCatalogs = feeService.getAllFeeCatalogs();

        ApiResponse<List<FeeCatalogDto>> response = ApiResponse.<List<FeeCatalogDto>>builder()
                .data(feeCatalogs)
                .message("Fee catalog list retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

//    @PutMapping("/update-overdue")
//    public ResponseEntity<ApiResponse<Void>> updateOverdueFees()
//    {
//        feeService.updatePendingFeesToOverdueForCurrentSession();
//
//        ApiResponse<Void> response = ApiResponse.<Void>builder()
//                .status(HttpStatus.OK.value())
//                .message("Pending fees updated to overdue successfully.")
//                .data(null)
//                .build();
//
//        return ResponseEntity.ok(response);
//    }

}
