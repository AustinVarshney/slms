package com.java.slms.dto;

import com.java.slms.util.FeeCatalogStatus;
import com.java.slms.util.FeeStatus;
import com.java.slms.util.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestDto
{

    private String panNumber;
    private String name;
    private String photo;
    private String password;
    private Integer classRollNumber;

    private FeeStatus feeStatus;
    private FeeCatalogStatus feeCatalogStatus;

    private String parentName;
    private String mobileNumber;

    private LocalDate dateOfBirth;
    private Gender gender;

    private String address;
    private String emergencyContact;
    private String bloodGroup;

    private LocalDate admissionDate;
    private String previousSchool;

    private Long sessionId;

    private Long userId;
    private Long classId;
    private String className;

    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;


}
