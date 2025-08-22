package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SubjectDto
{
    private Long id;
    private String subjectName;
    private Long classId;
    private String className;
    private Long teacherId;
    private Long sessionId;
    private String teacherName;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
