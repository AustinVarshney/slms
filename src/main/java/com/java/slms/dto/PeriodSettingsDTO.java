package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodSettingsDTO {
    private Long id;
    private Integer periodDuration; // in minutes
    private String schoolStartTime; // HH:MM format
    private Integer lunchPeriod; // after which period
    private Integer lunchDuration; // in minutes
    private Long schoolId;
    private Long sessionId;
}
