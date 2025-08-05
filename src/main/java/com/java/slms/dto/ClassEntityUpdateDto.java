package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntityUpdateDto
{
    private Long id;
    private String className;
    private int totalStudents;
    private List<StudentDto> students;

}
