package com.java.slms.dto;

import com.java.slms.util.Statuses;
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
    private Statuses status;
    private List<AttendanceDto> attendanceRecords;
    private String className;
    private Long classId;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
