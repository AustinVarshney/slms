package com.java.slms.dto;

import com.java.slms.util.QueryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StudentQueryResponse
{
    private Long teacherId;
    private String teacherName;
    private String subject;
    private String content;
    private String response;
    private QueryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
