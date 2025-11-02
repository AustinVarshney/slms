package com.java.slms.serviceImpl;

import com.java.slms.dto.BroadcastMessageDto;
import com.java.slms.dto.NotificationDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Notification;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.repository.NotificationRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService
{
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationDto notificationDto)
    {
        Notification notification = modelMapper.map(notificationDto, Notification.class);
        
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
        
        if (notification.getPriority() == null) {
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created for recipient: {}", savedNotification.getRecipientId());
        
        return modelMapper.map(savedNotification, NotificationDto.class);
    }

    @Override
    @Transactional
    public List<NotificationDto> broadcastMessage(BroadcastMessageDto broadcastDto, String senderName, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        
        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school"));
        
        // Generate unique broadcast ID
        String broadcastId = "BROADCAST_" + System.currentTimeMillis();
        
        List<Notification> notifications = new ArrayList<>();
        
        for (String recipientId : broadcastDto.getRecipientIds())
        {
            Notification notification = new Notification();
            notification.setTitle(broadcastDto.getTitle());
            notification.setMessage(broadcastDto.getMessage());
            notification.setRecipientId(recipientId);
            notification.setRecipientType(broadcastDto.getRecipientType());
            notification.setSenderName(senderName);
            notification.setSenderId(senderName); // Use email as sender ID
            notification.setBroadcastId(broadcastId);
            notification.setIsRead(false);
            notification.setPriority(broadcastDto.getPriority() != null ? 
                broadcastDto.getPriority() : Notification.NotificationPriority.MEDIUM);
            notification.setSchool(school);
            notification.setSession(activeSession);
            
            notifications.add(notification);
        }
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("Broadcast message sent to {} recipients by {}", savedNotifications.size(), senderName);
        
        return savedNotifications.stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByRecipient(String recipientId)
    {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId);
        
        return notifications.stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadNotificationsByRecipient(String recipientId)
    {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);
        
        return notifications.stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadCount(String recipientId)
    {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(Long notificationId)
    {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        return modelMapper.map(updatedNotification, NotificationDto.class);
    }

    @Override
    @Transactional
    public void markAllAsRead(String recipientId)
    {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);
        
        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for recipient: {}", unreadNotifications.size(), recipientId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId)
    {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted with id: {}", notificationId);
    }

    @Override
    public List<NotificationDto> getSentMessagesBySender(String senderId)
    {
        List<Notification> sentNotifications = notificationRepository
                .findBySenderIdOrderByCreatedAtDesc(senderId);
        
        return sentNotifications.stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationDto updateBroadcastMessage(String broadcastId, BroadcastMessageDto updateDto, String senderEmail, Long schoolId)
    {
        // Find all notifications with this broadcast ID
        List<Notification> broadcastNotifications = notificationRepository
                .findByBroadcastIdOrderByCreatedAtDesc(broadcastId);
        
        if (broadcastNotifications.isEmpty()) {
            throw new ResourceNotFoundException("Broadcast message not found with ID: " + broadcastId);
        }
        
        // Verify sender owns this broadcast
        String originalSenderId = broadcastNotifications.get(0).getSenderId();
        if (!originalSenderId.equals(senderEmail)) {
            throw new ResourceNotFoundException("You are not authorized to edit this broadcast");
        }
        
        // Update all notifications in this broadcast
        for (Notification notification : broadcastNotifications) {
            notification.setTitle(updateDto.getTitle());
            notification.setMessage(updateDto.getMessage());
            if (updateDto.getPriority() != null) {
                notification.setPriority(updateDto.getPriority());
            }
            // Mark as unread again to notify recipients of update
            notification.setIsRead(false);
            notification.setReadAt(null);
        }
        
        List<Notification> updatedNotifications = notificationRepository.saveAll(broadcastNotifications);
        log.info("Updated {} notifications in broadcast {} and marked as unread", updatedNotifications.size(), broadcastId);
        
        // Return the first notification as a representative
        return modelMapper.map(updatedNotifications.get(0), NotificationDto.class);
    }

    @Override
    public List<NotificationDto> getBroadcastMessagesByBroadcastId(String broadcastId)
    {
        List<Notification> notifications = notificationRepository
                .findByBroadcastIdOrderByCreatedAtDesc(broadcastId);
        
        return notifications.stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }
}
