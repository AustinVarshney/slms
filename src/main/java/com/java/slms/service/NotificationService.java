package com.java.slms.service;

import com.java.slms.dto.BroadcastMessageDto;
import com.java.slms.dto.NotificationDto;

import java.util.List;

public interface NotificationService
{
    NotificationDto createNotification(NotificationDto notificationDto);
    
    List<NotificationDto> broadcastMessage(BroadcastMessageDto broadcastDto, String senderName);
    
    List<NotificationDto> getNotificationsByRecipient(String recipientId);
    
    List<NotificationDto> getUnreadNotificationsByRecipient(String recipientId);
    
    Long getUnreadCount(String recipientId);
    
    NotificationDto markAsRead(Long notificationId);
    
    void markAllAsRead(String recipientId);
    
    void deleteNotification(Long notificationId);
}
