package com.java.slms.dto;

import com.java.slms.util.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest
{
    private String name;
    private String email;
    private String qualification;
    private UserStatus status;
    private String salaryGrade;
    private String designation;
    private String contactNumber;
    private LocalDate joiningDate;
    private Long schoolId;
    private String schoolName;
    private int allowedLeaves;
    private Long userId;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
