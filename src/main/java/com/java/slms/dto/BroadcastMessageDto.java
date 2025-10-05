package com.java.slms.dto;

import com.java.slms.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessageDto
{
    private String title;
    private String message;
    private List<String> recipientIds; // List of PAN numbers or emails
    private Notification.RecipientType recipientType;
    private Notification.NotificationPriority priority;
}
