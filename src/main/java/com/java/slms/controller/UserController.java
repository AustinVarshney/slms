package com.java.slms.controller;

import com.java.slms.dto.PasswordDto;
import com.java.slms.dto.UpdateUserDetails;
import com.java.slms.dto.UserRequest;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for managing users including password changes, updates and deletions")
public class UserController
{

    private final UserService userService;

    @Operation(
            summary = "Change user's password",
            description = "Change password for the user identified by userId.",
            parameters = {@Parameter(name = "userId", description = "ID of the user", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<RestResponse<Void>> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordDto password
    )
    {
        userService.changePassword(userId, password);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Password updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update user details",
            description = "Update details for the user identified by userId.",
            parameters = {@Parameter(name = "userId", description = "ID of the user", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User details updated successfully",
                            content = @Content(schema = @Schema(implementation = UserRequest.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PutMapping("/{userId}/update")
    public ResponseEntity<RestResponse<UserRequest>> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDetails updateUserDetails
    )
    {
        UserRequest updatedUser = userService.updateUserDetails(userId, updateUserDetails);
        return ResponseEntity.ok(
                RestResponse.<UserRequest>builder()
                        .data(updatedUser)
                        .message("User details updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Delete user",
            description = "Delete the user identified by userId.",
            parameters = {@Parameter(name = "userId", description = "ID of the user", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<RestResponse<Void>> deleteUser(@PathVariable Long userId)
    {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("User deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
