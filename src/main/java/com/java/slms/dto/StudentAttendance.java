package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StudentAttendance
{
    private String panNumber;
    private boolean isPresent;
}
