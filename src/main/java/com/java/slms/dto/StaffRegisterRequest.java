package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class StaffRegisterRequest
{
    private Long id;
    private String name;
    private String email;
    private String qualification;
    private String salaryGrade;
    private String contactNumber;
    private LocalDate joiningDate;
    private Set<String> roles;
    private String password;
    private String designation;
    private Integer allowedLeaves = 10; // Default value is 10
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
