package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
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
