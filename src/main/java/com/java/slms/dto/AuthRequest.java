package com.java.slms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest
{
    String email;
    String password;
}
