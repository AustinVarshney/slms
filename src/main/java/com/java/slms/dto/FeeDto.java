package com.java.slms.dto;

import com.java.slms.util.FeeCatalogStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeDto
{
    private Long id;
    private String studentPanNumber;
    private String studentName;
    private String className;
    private int year;
    private LocalDate dueDate;
    private Month month;
    private Double totalAmount;
    private Double amount;
    private Double remainingAmount;
    private String status;
    private Date paidOn;
}
