package com.java.slms.dto;

import com.java.slms.util.UserStatus;
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
    private Long userId;
    private List<Long> classId;
    private List<String> className;
    private List<String> subjectName;
    private List<Long> subjectId;
    private UserStatus status;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
