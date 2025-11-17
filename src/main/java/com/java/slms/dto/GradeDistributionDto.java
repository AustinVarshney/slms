package com.java.slms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDistributionDto
{
    private Long id;
    private String grade;
    private Double minPercentage;
    private Double maxPercentage;
    private String description;
}
