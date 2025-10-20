package com.java.slms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffLeaveAllowanceDto
{
    private Long staffId;
    private Long sessionId;
    private String sessionName;
    private int allowedLeaves;
    private int leavesUsed;
    private int remainingLeaves;
}