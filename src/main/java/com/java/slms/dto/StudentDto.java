package com.java.slms.dto;

import com.java.slms.util.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto
{
    private String panNumber;
    private String name;
    private String photo;
    private StudentStatus status;
    private List<AttendanceDto> attendanceRecords;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
