package com.java.slms.dto;

import com.java.slms.util.Statuses;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeacherDto
{
    private Long id;
    private String name;
    private String email;
    private String qualification;
    private List<Long> classId;
    private List<Long> subjectId;
    private String status;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
