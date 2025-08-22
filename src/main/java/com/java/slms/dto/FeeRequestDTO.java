package com.java.slms.dto;

import com.java.slms.util.FeeMonth;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeRequestDTO
{
    private String studentPanNumber;
    private Double amount;
    private FeeMonth month;
    private Long sessionId;
    private Long classId;
    private String receiptNumber;
}
