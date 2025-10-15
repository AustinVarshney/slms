package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoLectureDTO {
    private Long id;
    private String title;
    private String description;
    private String youtubeLink;
    private String subject;
    private String className;
    private String section;
    private Long teacherId;
    private String teacherName;
    private String duration;
    private String topic;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;
    
    private Boolean isActive;
}
