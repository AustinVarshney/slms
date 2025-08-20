package com.java.slms.dto;

import com.java.slms.util.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
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
}
