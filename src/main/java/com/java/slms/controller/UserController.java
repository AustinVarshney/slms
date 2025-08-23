package com.java.slms.controller;

import com.java.slms.dto.PasswordDto;
import com.java.slms.dto.UserRequest;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController
{

    private final UserService userService;

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordDto password
    )
    {
        userService.changePassword(userId, password);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Password updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse<UserRequest>> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UserRequest userRequest
    )
    {
        UserRequest updatedUser = userService.updateUserDetails(userId, userRequest);
        return ResponseEntity.ok(
                ApiResponse.<UserRequest>builder()
                        .data(updatedUser)
                        .message("User details updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId)
    {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("User deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
