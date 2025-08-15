package com.java.slms.service;

import com.java.slms.dto.UserRegistrationRequest;

import java.util.List;

public interface AdminService
{
    UserRegistrationRequest createAdmin(UserRegistrationRequest req);

    UserRegistrationRequest getAdmin(Long id);

    List<UserRegistrationRequest> getAllAdmins();
}