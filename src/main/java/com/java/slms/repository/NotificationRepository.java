package com.java.slms.repository;

import com.java.slms.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(String recipientId, Boolean isRead);
    
    Long countByRecipientIdAndIsRead(String recipientId, Boolean isRead);
    
    List<Notification> findByRecipientTypeOrderByCreatedAtDesc(Notification.RecipientType recipientType);
}
