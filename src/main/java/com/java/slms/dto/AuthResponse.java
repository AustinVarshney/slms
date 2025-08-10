package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse
{
    String accessToken;
    String tokenType;
    long expiresIn;
}
