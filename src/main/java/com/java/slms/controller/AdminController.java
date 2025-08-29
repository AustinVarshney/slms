package com.java.slms.controller;

import com.java.slms.dto.UserRequest;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
@Tag(name = "Admin Controller", description = "APIs for managing admins")
public class AdminController
{

    private final AdminService adminService;

    @Operation(
            summary = "Get all admins",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all admins retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<UserRequest>>> getAllAdmins()
    {
        List<UserRequest> admins = adminService.getAllAdmins();

        return ResponseEntity.ok(
                RestResponse.<List<UserRequest>>builder()
                        .data(admins)
                        .message("Total Admins - " + admins.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get active admins",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of active admins retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/active")
    public ResponseEntity<RestResponse<List<UserRequest>>> getActiveAdmins()
    {
        List<UserRequest> admins = adminService.getActiveAdmins();

        return ResponseEntity.ok(
                RestResponse.<List<UserRequest>>builder()
                        .data(admins)
                        .message("Active Admins - " + admins.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get admin by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or admin not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<UserRequest>> getAdminById(@PathVariable Long id)
    {
        UserRequest admin = adminService.getAdminById(id);

        return ResponseEntity.ok(
                RestResponse.<UserRequest>builder()
                        .data(admin)
                        .message("Admin fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Deactivate admin by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin deactivated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or admin already inactive", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> inActiveAdmin(@PathVariable Long id)
    {
        adminService.inActiveAdmin(id);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Admin Deactivated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
