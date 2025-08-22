package com.java.slms.dto;

import com.java.slms.util.UserStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentStatusRequest
{
    private List<String> panNumbers;
    private UserStatus status;
}
