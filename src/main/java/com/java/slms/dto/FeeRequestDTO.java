package com.java.slms.dto;

import com.java.slms.util.FeeMonth;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeRequestDTO
{
    private String studentPanNumber;
    private Long feeStructureId;
    private Double amountPaid;
    private FeeMonth month;
    private String paymentHistory;
}
