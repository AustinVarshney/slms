package com.java.slms.service;

import com.java.slms.dto.UserRequest;

import java.util.List;

public interface AdminService
{
    UserRequest createAdmin(UserRequest adminDto);

    UserRequest getAdminById(Long id);

    List<UserRequest> getAllAdmins();

    List<UserRequest> getActiveAdmins();

    UserRequest updateAdmin(Long id, UserRequest adminDto);

    void deleteAdmin(Long id);
}