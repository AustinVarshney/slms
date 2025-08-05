package com.java.slms.dto;

import com.java.slms.model.Student;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntityDto
{
    private Long id;
    private String className;
    private int totalStudents;
    private List<StudentDto> students;
    private List<SubjectDto> subjects;
}
