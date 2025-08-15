package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeeStaffRequest
{
    private Long id;
    private String name;
    private String password;
    private String department;
    private Long userId;
}
