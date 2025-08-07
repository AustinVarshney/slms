package com.java.slms.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SubjectDto
{
    private Long id;
    private String subjectName;
    private Long classId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
