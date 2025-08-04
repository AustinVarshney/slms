package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<AttendanceDto> attendanceRecords;
    private Date createdAt;
    private Date updatedAt;
}
