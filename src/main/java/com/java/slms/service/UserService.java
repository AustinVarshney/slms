package com.java.slms.service;

import com.java.slms.dto.UserRequest;

public interface UserService
{
    void changePassword(Long userId, String password);

    void deleteUser(Long userId);

    UserRequest updateUserDetails(Long userId, UserRequest userRequest);

}

