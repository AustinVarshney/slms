package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NonTeachingStaffRequest
{
    private Long id;
    private String name;
    private String password;
    private String department;
    private Long userId;
}
