package com.java.slms.dto;

import lombok.Data;

import java.util.Date;

@Data
public class StudentForAttendance
{
    private String panNumber;
    private String name;
    private String status;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
