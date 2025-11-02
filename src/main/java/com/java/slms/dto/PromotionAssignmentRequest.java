package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionAssignmentRequest {
    private String studentPan;
    private Long toClassId;
    private String remarks;
    private Boolean isGraduated;
    private Boolean isDetained;  // Student stays in same class but moves to new session
}
