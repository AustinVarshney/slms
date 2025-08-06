package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamDto
{
    private Long id;
    private String name;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date examDate;
    private Long classId;
    private String className;
}
