package com.java.slms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolRequestDto
{
    private String schoolName;
    private String schoolEmail;
    private String schoolWebsite;
    private String schoolAddress;
    private String schoolContactNumber;
    private String schoolLogo; // Cloudinary URL
    private String schoolTagline;
}