package com.java.slms.dto;

import lombok.Data;

import java.util.Date;

@Data
public class StudentAttendance
{
    private String panNumber;
    private boolean isPresent;
}
