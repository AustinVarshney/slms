package com.java.slms.dto;

import com.java.slms.util.FeeCatalogStatus;
import com.java.slms.util.FeeStatus;
import com.java.slms.util.Gender;
import com.java.slms.util.UserStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDto
{
    private String panNumber;
    private String name;
    private String photo;

    private Long classId;
    private String className;
    private String currentClass; // Just the class number (e.g., "1", "10")
    private String section; // Just the section (e.g., "A", "B")
    private Integer classRollNumber;
    private UserStatus status;

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

    private String sessionName;
    private Long sessionId;

    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
}
