package com.java.slms.repository;

import com.java.slms.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    // Query methods with session filtering
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.session.id = :sessionId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndSessionIdOrderByCreatedAtDesc(@Param("recipientId") String recipientId, @Param("sessionId") Long sessionId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = :isRead AND n.session.id = :sessionId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndIsReadAndSessionIdOrderByCreatedAtDesc(@Param("recipientId") String recipientId, @Param("isRead") Boolean isRead, @Param("sessionId") Long sessionId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = :isRead AND n.session.id = :sessionId")
    Long countByRecipientIdAndIsReadAndSessionId(@Param("recipientId") String recipientId, @Param("isRead") Boolean isRead, @Param("sessionId") Long sessionId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientType = :recipientType AND n.session.id = :sessionId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientTypeAndSessionIdOrderByCreatedAtDesc(@Param("recipientType") Notification.RecipientType recipientType, @Param("sessionId") Long sessionId);
    
    @Query("SELECT n FROM Notification n WHERE n.senderId = :senderId AND n.session.id = :sessionId ORDER BY n.createdAt DESC")
    List<Notification> findBySenderIdAndSessionIdOrderByCreatedAtDesc(@Param("senderId") String senderId, @Param("sessionId") Long sessionId);
    
    @Query("SELECT n FROM Notification n WHERE n.broadcastId = :broadcastId AND n.session.id = :sessionId ORDER BY n.createdAt DESC")
    List<Notification> findByBroadcastIdAndSessionIdOrderByCreatedAtDesc(@Param("broadcastId") String broadcastId, @Param("sessionId") Long sessionId);
    
    @Query("SELECT DISTINCT n.broadcastId, n.title, n.message, n.priority, n.createdAt, n.senderName " +
           "FROM Notification n WHERE n.senderId = :senderId AND n.broadcastId IS NOT NULL AND n.session.id = :sessionId " +
           "GROUP BY n.broadcastId, n.title, n.message, n.priority, n.createdAt, n.senderName " +
           "ORDER BY n.createdAt DESC")
    List<Object[]> findDistinctBroadcastsBySenderIdAndSessionId(@Param("senderId") String senderId, @Param("sessionId") Long sessionId);

    // Backward compatibility - without session filtering
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(String recipientId, Boolean isRead);
    
    Long countByRecipientIdAndIsRead(String recipientId, Boolean isRead);
    
    List<Notification> findByRecipientTypeOrderByCreatedAtDesc(Notification.RecipientType recipientType);
    
    List<Notification> findBySenderIdOrderByCreatedAtDesc(String senderId);
    
    List<Notification> findByBroadcastIdOrderByCreatedAtDesc(String broadcastId);
    
    @Query("SELECT DISTINCT n.broadcastId, n.title, n.message, n.priority, n.createdAt, n.senderName " +
           "FROM Notification n WHERE n.senderId = :senderId AND n.broadcastId IS NOT NULL " +
           "GROUP BY n.broadcastId, n.title, n.message, n.priority, n.createdAt, n.senderName " +
           "ORDER BY n.createdAt DESC")
    List<Object[]> findDistinctBroadcastsBySenderId(@Param("senderId") String senderId);
}
