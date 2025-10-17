package com.java.slms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassRequestDto
{
    private String className;
    private Double feesAmount;
    private Long schoolId;
    private Long classTeacherId;
}
