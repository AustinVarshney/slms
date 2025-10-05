package com.java.slms.controller;

import com.java.slms.dto.BroadcastMessageDto;
import com.java.slms.dto.NotificationDto;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "APIs for managing notifications and broadcasts")
public class NotificationController
{
    private final NotificationService notificationService;

    @Operation(summary = "Broadcast message to multiple recipients (Admin only)")
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<NotificationDto>>> broadcastMessage(
            @RequestBody BroadcastMessageDto broadcastDto)
    {
        String senderName = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<NotificationDto> notifications = notificationService.broadcastMessage(broadcastDto, senderName);
        
        return ResponseEntity.ok(
                RestResponse.<List<NotificationDto>>builder()
                        .data(notifications)
                        .message("Broadcast message sent to " + notifications.size() + " recipients successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get all notifications for current user")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<NotificationDto>>> getMyNotifications()
    {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<NotificationDto> notifications = notificationService.getNotificationsByRecipient(userId);
        
        return ResponseEntity.ok(
                RestResponse.<List<NotificationDto>>builder()
                        .data(notifications)
                        .message("Notifications fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get unread notifications for current user")
    @GetMapping("/me/unread")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<List<NotificationDto>>> getMyUnreadNotifications()
    {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsByRecipient(userId);
        
        return ResponseEntity.ok(
                RestResponse.<List<NotificationDto>>builder()
                        .data(notifications)
                        .message("Unread notifications fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Get unread notification count for current user")
    @GetMapping("/me/unread/count")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Long>> getUnreadCount()
    {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Long count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(
                RestResponse.<Long>builder()
                        .data(count)
                        .message("Unread count fetched successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<NotificationDto>> markAsRead(@PathVariable Long notificationId)
    {
        NotificationDto notification = notificationService.markAsRead(notificationId);
        
        return ResponseEntity.ok(
                RestResponse.<NotificationDto>builder()
                        .data(notification)
                        .message("Notification marked as read")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Mark all notifications as read for current user")
    @PutMapping("/me/read-all")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> markAllAsRead()
    {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .data(null)
                        .message("All notifications marked as read")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(summary = "Delete notification")
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteNotification(@PathVariable Long notificationId)
    {
        notificationService.deleteNotification(notificationId);
        
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .data(null)
                        .message("Notification deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
