package com.java.slms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest
{
    private String name;
    private String email;
    private String qualification;
    private Long userId;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
