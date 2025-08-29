package com.java.slms.service;

import com.java.slms.dto.PasswordDto;
import com.java.slms.dto.UpdateUserDetails;
import com.java.slms.dto.UserRequest;

public interface UserService
{
    void changePassword(Long userId, PasswordDto password);

    void deleteUser(Long userId);

    UserRequest updateUserDetails(Long userId, UpdateUserDetails updateUserDetails);

}

