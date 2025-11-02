package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponseDto
{
    private Long id;
    private String schoolName;
    private String schoolEmail;
    private String schoolWebsite;
    private String schoolAddress;
    private String schoolContactNumber;
    private String schoolLogo; // Cloudinary URL
    private String schoolTagline;
}