package com.java.slms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntityUpdateDto
{
    private Long id;
    private String className;
    private int totalStudents;
    private List<StudentRequestDto> students;

}
