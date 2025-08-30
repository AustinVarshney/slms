package com.java.slms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassRequestDto
{
    private String className;
    private Long sessionId;
    private Double feesAmount;
    private Long classTeacherId;
}
