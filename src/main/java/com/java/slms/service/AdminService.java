package com.java.slms.service;

import com.java.slms.dto.UserRequest;
import com.java.slms.model.Admin;

public interface AdminService
{
    UserRequest createAdmin(UserRequest adminDto);

    UserRequest getAdminDetails(String email, Long schoolId);

    Admin getAdminInfo(String email, Long schoolId);


}