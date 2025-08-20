package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructureRequestDTO
{
    private Double feesAmount;
    private Long classId;
    private Long sessionId;
}
