package com.java.slms.controller;

import com.java.slms.dto.UserRequest;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.NonTeachingStaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nts")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Non Teaching Staff Controller", description = "APIs for managing non-teaching staff")
public class NonTeachingStaffController
{
    private final NonTeachingStaffService nonTeachingStaffService;

    // Get Non-Teaching Staff by ID
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<UserRequest>> getFeeStaffById(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId)
    {

        UserRequest feeStaff = nonTeachingStaffService.getFeeStaffById(id, schoolId);

        return ResponseEntity.ok(
                RestResponse.<UserRequest>builder()
                        .data(feeStaff)
                        .message("Non Teaching Staff fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    // Get all Non-Teaching Staff for a specific school
    @GetMapping
    public ResponseEntity<RestResponse<List<UserRequest>>> getAllFeeStaff(
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<UserRequest> list = nonTeachingStaffService.getAllFeeStaff(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<UserRequest>>builder()
                        .data(list)
                        .message("Total Non Teaching Staff - " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    // Get active Non-Teaching Staff for a specific school
    @GetMapping("/active")
    public ResponseEntity<RestResponse<List<UserRequest>>> getActiveFeeStaff(
            @RequestAttribute("schoolId") Long schoolId)
    {

        List<UserRequest> list = nonTeachingStaffService.getActiveFeeStaff(schoolId);

        return ResponseEntity.ok(
                RestResponse.<List<UserRequest>>builder()
                        .data(list)
                        .message("Active Non Teaching Staff - " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    // Deactivate Non-Teaching Staff
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteFeeStaff(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId)
    {

        nonTeachingStaffService.inActiveNonTeachingStaff(id, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Non Teaching Staff Deactivated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}