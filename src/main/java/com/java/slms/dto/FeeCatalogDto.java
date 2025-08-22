package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeeCatalogDto
{
    private String studentId;
    private List<MonthlyFeeDto> monthlyFees;
    private double totalAmount;
    private double totalPaid;
    private double totalPending;
    private double totalOverdue;
}
