package com.java.slms.controller;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Fee Controller", description = "APIs for managing fee payments and fee catalogs")
public class FeeController
{
    private final FeeService feeService;

    @Operation(
            summary = "Pay fees for a student",
            description = "Marks fees as paid for a student based on the fee request details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fees updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid payment data or payment already made", content = @Content)
            }
    )
    @PutMapping("/pay")
    public ResponseEntity<RestResponse<?>> payFees(@RequestBody FeeRequestDTO requestDto)
    {

        feeService.payFeesOfStudent(requestDto);

        return ResponseEntity.ok(
                RestResponse.builder()
                        .data(null)
                        .message("Fees updated successfully.")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get all fee catalogs in active session",
            description = "Retrieves all fee catalogs for students in the active session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee catalog list retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/catalogs")
    public ResponseEntity<RestResponse<List<FeeCatalogDto>>> getAllFeeCatalogs()
    {
        List<FeeCatalogDto> feeCatalogs = feeService.getAllFeeCatalogsInActiveSesssion();

        RestResponse<List<FeeCatalogDto>> response = RestResponse.<List<FeeCatalogDto>>builder()
                .data(feeCatalogs)
                .message("Fee catalog list retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update overdue fees",
            description = "Updates all pending fees and marks them as overdue.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pending fees updated to overdue successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PutMapping("/update-overdue")
    public ResponseEntity<RestResponse<Void>> updateOverdueFees()
    {
        feeService.markPendingFeesAsOverdue();

        RestResponse<Void> response = RestResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Pending fees updated to overdue successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get fee catalog by student PAN",
            description = "Fetches the fee catalog for a student identified by PAN number.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee catalog retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid PAN or student not found", content = @Content)
            }
    )
    @GetMapping("/catalogs/{panNumber}")
    public ResponseEntity<RestResponse<FeeCatalogDto>> getFeeCatalogByStudentPan(@PathVariable String panNumber)
    {
        FeeCatalogDto catalog = feeService.getFeeCatalogByStudentPanNumber(panNumber);
        RestResponse<FeeCatalogDto> response = RestResponse.<FeeCatalogDto>builder()
                .data(catalog)
                .message("Fee catalog retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get fee catalog for current logged-in student",
            description = "Fetches the fee catalog for the currently authenticated student.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee catalog retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Unauthorized or student not found", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/catalogs/me")
    public ResponseEntity<RestResponse<FeeCatalogDto>> getFeeCatalogByCurrentStudent()
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        FeeCatalogDto catalog = feeService.getFeeCatalogByStudentPanNumber(panNumber);
        RestResponse<FeeCatalogDto> response = RestResponse.<FeeCatalogDto>builder()
                .data(catalog)
                .message("Fee catalog retrieved successfully.")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }
}
