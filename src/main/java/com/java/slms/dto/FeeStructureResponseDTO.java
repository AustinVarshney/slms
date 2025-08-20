package com.java.slms.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructureResponseDTO
{
    private Long id;
    private Double feesAmount;
    private Long classId;
    private String className;
    private Long sessionId;
    private String sessionName;
}
