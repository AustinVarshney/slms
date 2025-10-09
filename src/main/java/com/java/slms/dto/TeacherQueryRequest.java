package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherQueryRequest
{
    private Long adminId;
    private String subject;
    private String content;
}
