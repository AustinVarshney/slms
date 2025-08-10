package com.java.slms.dto;

import lombok.Data;

@Data
public class StaffRegisterRequest
{
    String email;
    String password;
    String role;
}
