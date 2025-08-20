package com.java.slms.dto;

import lombok.Data;

import java.util.List;

@Data
public class FeeCatalogDto
{
    private String studentId;
    private List<MonthlyFeeDto> monthlyFees;
    private double totalAmount;
    private double totalPaid;
    private double totalPending;
    private double totalOverdue;
}
