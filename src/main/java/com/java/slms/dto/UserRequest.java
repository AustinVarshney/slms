package com.java.slms.dto;

import com.java.slms.util.UserStatuses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest
{
    private String name;
    private String email;
    private String qualification;
    private UserStatuses status;
    private Long userId;
    private Date createdAt;
    private Date deletedAt;
    private Date updatedAt;
}
