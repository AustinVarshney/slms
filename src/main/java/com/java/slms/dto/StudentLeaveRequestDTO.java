package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentLeaveRequestDTO
{
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String proofImage; // Optional: Cloudinary URL for proof image
}
