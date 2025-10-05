package com.java.slms.dto;

import com.java.slms.util.Gender;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateStudentInfo
{
    private String name;
    private String photo;
    private String parentName;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String emergencyContact;
    private String bloodGroup;
    private String previousSchool;
    private String className; // Format: "1-A", "10-B", etc.
    private Long classId; // Direct class ID if provided
}
