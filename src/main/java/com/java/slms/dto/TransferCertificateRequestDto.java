package com.java.slms.dto;

import com.java.slms.util.RequestStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferCertificateRequestDto
{
    private Long id;
    private String studentPanNumber;
    private String studentName;
    private LocalDate requestDate;
    private RequestStatus status;
    private String reason;
    private String adminReply;
    private LocalDate adminActionDate;
    private Long sessionId;
    private String sessionName;
    private Long classId;
    private String className;
    private Long schoolId;

}