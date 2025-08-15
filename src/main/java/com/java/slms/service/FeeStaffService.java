package com.java.slms.service;

import com.java.slms.dto.FeeStaffRequest;
import com.java.slms.dto.UserRegistrationRequest;

import java.util.List;

public interface FeeStaffService
{
    UserRegistrationRequest createFeeStaff(UserRegistrationRequest req);

    UserRegistrationRequest getFeeStaff(Long id);

    List<UserRegistrationRequest> getAllFeeStaff();
}
