package com.java.slms.dto;

import com.java.slms.util.UserStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TeacherDto
{
    private Long id;
    private String name;
    private String email;
    private String qualification;
    private String salaryGrade;
    private String contactNumber;
    private Long userId;
    private String designation;
    private LocalDate joiningDate;
    private List<Long> classId;
    private List<String> className;
    private List<String> subjectName;
    private List<Long> subjectId;
    private UserStatus status;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
