package com.java.slms.dto;

import com.java.slms.util.QueryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StudentQueryResponse
{
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String subject;
    private String content;
    private String response;
    private QueryStatus status;
    private Long schoolId;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
