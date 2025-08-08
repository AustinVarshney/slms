package com.java.slms.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeDto
{
    private Long id;
    private String studentPanNumber;
    private String studentName;
    private String className;
    private String feeType;
    private Double totalAmount;
    private Double amountPaid;
    private Double remainingAmount;
    private String status;
    private Date paidOn;
}
