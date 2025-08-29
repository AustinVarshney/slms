package com.java.slms.controller;

import com.java.slms.dto.FeeStructureRequestDTO;
import com.java.slms.dto.FeeStructureResponseDTO;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fee-structures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
@Slf4j
@Tag(name = "Fee Structure Controller", description = "APIs to manage fee structures")
public class FeeStructureController
{

    private final FeeStructureService feeStructureService;

    @Operation(
            summary = "Create a new fee structure",
            description = "Creates a new fee structure entry for a class and session.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Fee structure created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or fee structure already exists", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<RestResponse<FeeStructureResponseDTO>> createFeeStructure(
            @RequestBody FeeStructureRequestDTO dto)
    {
        log.info("Request to create FeeStructure for ClassId: {}", dto.getClassId());
        FeeStructureResponseDTO saved = feeStructureService.createFeeStructure(dto);
        log.info("FeeStructure created with ID: {}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<FeeStructureResponseDTO>builder()
                        .data(saved)
                        .message("Fee Structure created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update an existing fee structure",
            description = "Updates the fee structure details by ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the fee structure to update", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structure updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or fee structure not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<FeeStructureResponseDTO>> updateFeeStructure(
            @PathVariable Long id,
            @RequestBody FeeStructureRequestDTO dto)
    {
        log.info("Request to update FeeStructure with ID: {}", id);
        FeeStructureResponseDTO updated = feeStructureService.updateFeeStructure(id, dto);
        log.info("FeeStructure updated: ID {}", id);

        return ResponseEntity.ok(
                RestResponse.<FeeStructureResponseDTO>builder()
                        .data(updated)
                        .message("Fee Structure updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get fee structure by ID",
            description = "Fetch fee structure details by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "Fee structure ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structure fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or fee structure not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<FeeStructureResponseDTO>> getFeeStructureById(@PathVariable Long id)
    {
        log.info("Fetching FeeStructure with ID: {}", id);
        FeeStructureResponseDTO dto = feeStructureService.getFeeStructureById(id);
        return ResponseEntity.ok(
                RestResponse.<FeeStructureResponseDTO>builder()
                        .data(dto)
                        .message("Fee Structure fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get fee structures by class ID",
            description = "Fetch all fee structures associated with a specific class.",
            parameters = {
                    @Parameter(name = "classId", description = "Class ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structures fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/class/{classId}")
    public ResponseEntity<RestResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByClassId(@PathVariable Long classId)
    {
        log.info("Fetching FeeStructures for ClassId: {}", classId);
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByClassId(classId);
        return ResponseEntity.ok(
                RestResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched for Class ID: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get fee structures by fee type",
            description = "Fetch all fee structures of a specific fee type.",
            parameters = {
                    @Parameter(name = "feeType", description = "Fee type string", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structures fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid fee type", content = @Content)
            }
    )
    @GetMapping("/type/{feeType}")
    public ResponseEntity<RestResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByFeeType(@PathVariable String feeType)
    {
        log.info("Fetching FeeStructures for FeeType: {}", feeType);
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByFeeType(feeType);
        return ResponseEntity.ok(
                RestResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched for type: " + feeType)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get fee structures by due date range",
            description = "Fetch all fee structures with due dates within the given range.",
            parameters = {
                    @Parameter(name = "startDate", description = "Start date in yyyy-MM-dd", required = true),
                    @Parameter(name = "endDate", description = "End date in yyyy-MM-dd", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structures fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content)
            }
    )
    @GetMapping("/due-date-range")
    public ResponseEntity<RestResponse<List<FeeStructureResponseDTO>>> getFeeStructuresByDueDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate)
    {
        log.info("Fetching FeeStructures between due dates: {} and {}", startDate, endDate);
        List<FeeStructureResponseDTO> list = feeStructureService.getFeeStructuresByDueDateRange(startDate, endDate);
        return ResponseEntity.ok(
                RestResponse.<List<FeeStructureResponseDTO>>builder()
                        .data(list)
                        .message("Fee Structures fetched between due dates: " + startDate + " and " + endDate)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete a fee structure",
            description = "Deletes a fee structure by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the fee structure to delete", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fee structure deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or fee structure not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteFeeStructure(@PathVariable Long id)
    {
        log.info("Deleting FeeStructure with ID: {}", id);
        feeStructureService.deleteFeeStructure(id);
        log.info("Deleted FeeStructure with ID: {}", id);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Fee Structure deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
