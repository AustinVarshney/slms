package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StudentAttendance
{
    private String panNumber;
    
    // Use 'present' field name instead of 'isPresent' to avoid Lombok getter/setter issues
    // This will generate isPresent() getter and setPresent() setter
    // Jackson will correctly map JSON "isPresent" to this field
    @JsonProperty("isPresent")
    private boolean present;
}
