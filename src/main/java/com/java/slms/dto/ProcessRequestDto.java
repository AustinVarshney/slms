package com.java.slms.dto;

import com.java.slms.util.RequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestDto
{
    private String adminReply;
    private RequestStatus decision;  // APPROVED or REJECTED
}
