package com.java.slms.dto;

import com.java.slms.util.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveActionRequest
{
    private LeaveStatus status;
    private String responseMessage;
}
