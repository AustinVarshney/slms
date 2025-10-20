package com.java.slms.dto;

import com.java.slms.util.QueryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TeacherQueryResponse
{
    private Long adminId;
    private String adminName;
    private String subject;
    private String content;
    private String response;
    private Long schoolId;
    private QueryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
