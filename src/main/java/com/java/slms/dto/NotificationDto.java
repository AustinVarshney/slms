package com.java.slms.dto;

import com.java.slms.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto
{
    private Long id;
    private String title;
    private String message;
    private String recipientId;
    private Notification.RecipientType recipientType;
    private String senderName;
    private String senderId;
    private String broadcastId;
    private Boolean isRead;
    private Notification.NotificationPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
