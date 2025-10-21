package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.slms.util.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class StudentLeaveResponse
{
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private int daysRequested;

    private String reason;
    private String proofImage; // Optional: Cloudinary URL for proof image
    private String classTeacherResponse;

    private LeaveStatus status;

    private LocalDateTime processedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Kolkata")
    private Date createdAt; // When the leave request was submitted

    private String classTeacherName;
    private String sessionName;
}
