package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegisterResponseDto
{
    private String name;
    private String email;
    private String contactNumber;
    private String qualification;
    private String role;
    private String schoolName;
    private String schoolEmail;
    private String schoolWebsite;
    private String schoolContactNumber;
    private String schoolAddress;
    private Long schoolId;
}
