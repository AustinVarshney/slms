package com.java.slms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentScore
{
    private String studentPanNumber;
    private Double marks;
    private String grade;
}
