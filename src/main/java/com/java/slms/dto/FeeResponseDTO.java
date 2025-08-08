package com.java.slms.dto;

import com.java.slms.util.FeeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeResponseDTO
{
    private Long id;
    private String studentPanNumber;
    private String studentName;
    private Long feeStructureId;
    private String feeType;
    private Double totalAmount;
    private Double amountPaid;
    private Double remainingAmount;
    private FeeStatus status;
    private Date paidOn;
}
