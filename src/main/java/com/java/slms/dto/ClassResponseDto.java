package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long schoolId;
}
