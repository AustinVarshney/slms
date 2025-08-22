package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassInfoResponse
{
    private Long id;
    private String className;
    private Long sessionId;
    private String sessionName;
    private Double feesAmount;
    private int totalStudents;
    private Double feeCollectionRate;
    private List<StudentResponseDto> students;
}
