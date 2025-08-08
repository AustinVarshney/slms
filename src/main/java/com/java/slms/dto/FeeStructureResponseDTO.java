package com.java.slms.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructureResponseDTO
{
    private Long id;
    private String feeType;
    private Double defaultAmount;
    private Date dueDate;
    private Long classId;
    private String className;
}
