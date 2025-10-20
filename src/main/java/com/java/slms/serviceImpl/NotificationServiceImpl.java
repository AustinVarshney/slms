package com.java.slms.serviceImpl;

import com.java.slms.dto.BroadcastMessageDto;
import com.java.slms.dto.NotificationDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Notification;
import com.java.slms.model.School;
import com.java.slms.repository.NotificationRepository;
import com.java.slms.repository.SchoolRepository;
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
        
        List<Notification> notifications = new ArrayList<>();
        
        for (String recipientId : broadcastDto.getRecipientIds())
        {
            Notification notification = new Notification();
            notification.setTitle(broadcastDto.getTitle());
            notification.setMessage(broadcastDto.getMessage());
            notification.setRecipientId(recipientId);
            notification.setRecipientType(broadcastDto.getRecipientType());
            notification.setSenderName(senderName);
            notification.setIsRead(false);
            notification.setPriority(broadcastDto.getPriority() != null ? 
                broadcastDto.getPriority() : Notification.NotificationPriority.MEDIUM);
            notification.setSchool(school);
            
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
}
