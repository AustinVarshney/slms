package com.java.slms.controller;

import com.java.slms.dto.UserRequest;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.FeeStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feestaff")
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class FeeStaffController {

    private final FeeStaffService feeStaffService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserRequest>> createFeeStaff(
            @RequestBody UserRequest feeStaffDto) {
        UserRequest created = feeStaffService.createFeeStaff(feeStaffDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserRequest>builder()
                        .data(created)
                        .message("FeeStaff created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRequest>> getFeeStaffById(@PathVariable Long id) {
        UserRequest feeStaff = feeStaffService.getFeeStaffById(id);

        return ResponseEntity.ok(
                ApiResponse.<UserRequest>builder()
                        .data(feeStaff)
                        .message("FeeStaff fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserRequest>>> getAllFeeStaff() {
        List<UserRequest> list = feeStaffService.getAllFeeStaff();

        return ResponseEntity.ok(
                ApiResponse.<List<UserRequest>>builder()
                        .data(list)
                        .message("Total FeeStaff - " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserRequest>>> getActiveFeeStaff() {
        List<UserRequest> list = feeStaffService.getActiveFeeStaff();

        return ResponseEntity.ok(
                ApiResponse.<List<UserRequest>>builder()
                        .data(list)
                        .message("Active FeeStaff - " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRequest>> updateFeeStaff(
            @PathVariable Long id,
            @RequestBody UserRequest feeStaffDto) {
        UserRequest updated = feeStaffService.updateFeeStaff(id, feeStaffDto);

        return ResponseEntity.ok(
                ApiResponse.<UserRequest>builder()
                        .data(updated)
                        .message("FeeStaff updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStaff(@PathVariable Long id) {
        feeStaffService.deleteFeeStaff(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("FeeStaff deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
