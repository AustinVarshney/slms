package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegisterRequestDto
{
    private String email;
    private String password;
    private String contactNumber;
    private String designation;
    private String qualification;
    private String name;
    private String schoolName;
    private String schoolEmail;
    private String schoolWebsite;
    private String schoolContactNumber;
    private String schoolAddress;
    private String schoolLogo;
    private String schoolTagline;
}
