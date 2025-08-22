package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Getter
@Setter
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
    private Double maximumMarks;
    private Double passingMarks;
}
