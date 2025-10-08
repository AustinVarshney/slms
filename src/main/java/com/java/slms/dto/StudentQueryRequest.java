package com.java.slms.dto;

import com.java.slms.model.Student;
import com.java.slms.model.Teacher;
import com.java.slms.util.QueryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StudentQueryRequest
{
    private Long teacherId;
    private String subject;
    private String content;
}
