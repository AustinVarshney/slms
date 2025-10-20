package com.java.slms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId; // PAN number for students, email for teachers/staff

    @Column(name = "recipient_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecipientType recipientType;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    public enum RecipientType {
        STUDENT,
        TEACHER,
        STAFF,
        ADMIN
    }

    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}
