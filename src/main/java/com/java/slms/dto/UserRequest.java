package com.java.slms.dto;

import com.java.slms.util.UserStatus;
import lombok.*;

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
    private Long userId;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
