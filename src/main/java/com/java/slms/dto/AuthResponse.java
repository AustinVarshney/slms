package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse
{
    String accessToken;
    String tokenType;
    long expiresIn;
}
