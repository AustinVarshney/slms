package com.java.slms.dto;

import com.java.slms.util.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentStatusRequest
{
    private List<String> panNumbers;
    private UserStatus status;
}
