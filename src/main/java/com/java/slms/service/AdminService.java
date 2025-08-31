package com.java.slms.service;

import com.java.slms.dto.UserRequest;
import com.java.slms.model.Admin;

import java.util.List;

public interface AdminService
{
    UserRequest createAdmin(UserRequest adminDto);

    UserRequest getAdminById(Long id);

    List<UserRequest> getAllAdmins();

    List<UserRequest> getActiveAdmins();

    Admin getActiveAdminByEmail(String email);

    UserRequest updateAdmin(Long id, UserRequest adminDto);

    void inActiveAdmin(Long id);
}