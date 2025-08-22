package com.java.slms.controller;

import com.java.slms.dto.UserRequest;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class AdminController
{

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserRequest>>> getAllAdmins()
    {
        List<UserRequest> admins = adminService.getAllAdmins();

        return ResponseEntity.ok(
                ApiResponse.<List<UserRequest>>builder()
                        .data(admins)
                        .message("Total Admins - " + admins.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserRequest>>> getActiveAdmins()
    {
        List<UserRequest> admins = adminService.getActiveAdmins();

        return ResponseEntity.ok(
                ApiResponse.<List<UserRequest>>builder()
                        .data(admins)
                        .message("Active Admins - " + admins.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRequest>> getAdminById(@PathVariable Long id)
    {
        UserRequest admin = adminService.getAdminById(id);

        return ResponseEntity.ok(
                ApiResponse.<UserRequest>builder()
                        .data(admin)
                        .message("Admin fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> inActiveAdmin(@PathVariable Long id)
    {
        adminService.inActiveAdmin(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Admin Deactivated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
