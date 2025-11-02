package com.java.slms.dto;

import com.java.slms.util.PromotionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentPromotionDto {
    private Long id;
    private String studentPan;
    private String studentName;
    private Long fromClassId;
    private String fromClassName;
    private Long toClassId;
    private String toClassName;
    private Long fromSessionId;
    private String fromSessionName;
    private Long toSessionId;
    private String toSessionName;
    private Long assignedByTeacherId;
    private String assignedByTeacherName;
    private PromotionStatus status;
    private String remarks;
    private Boolean isGraduated;
    private Boolean isDetained;  // True if student is detained in same class
    private Long schoolId;
}
