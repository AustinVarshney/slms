package com.java.slms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponseDto
{
    private Long id;
    private String className;
    private Long sessionId;
    private String sessionName;
    private Double feesAmount;
    private Long classTeacherId;
    private String classTeacherName;
    private int totalStudents;
}
