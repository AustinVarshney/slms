package com.java.slms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.slms.model.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_lectures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoLecture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String youtubeLink;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false)
    private String className;
    
    @Column(nullable = false)
    private String section;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    @Column(name = "teacher_name")
    private String teacherName;
    
    @Column(name = "duration")
    private String duration; // e.g., "15:30" or "1h 30m"
    
    @Column(name = "topic")
    private String topic;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
}
