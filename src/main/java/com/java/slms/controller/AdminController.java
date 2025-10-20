package com.java.slms.controller;

import com.java.slms.dto.UserRequest;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin Controller", description = "APIs for managing admins")
public class AdminController
{
    private final AdminService adminService;

    @Operation(
            summary = "Get Admin Details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all admins retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<UserRequest>> getAdminDetails(@RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserRequest admin = adminService.getAdminDetails(email, schoolId);

        return ResponseEntity.ok(
                RestResponse.<UserRequest>builder()
                        .data(admin)
                        .message("Admin Details fetched Successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
