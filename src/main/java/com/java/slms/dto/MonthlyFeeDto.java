package com.java.slms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MonthlyFeeDto
{
    private String month;
    private int year;
    private double amount;
    private LocalDate dueDate;
    private String status;
    private LocalDate paymentDate;
    private String receiptNumber;
}
