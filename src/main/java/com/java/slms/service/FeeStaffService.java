package com.java.slms.service;

import com.java.slms.dto.UserRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FeeStaffService
{

    UserRequest createFeeStaff(UserRequest feeStaffDto);

    UserRequest getFeeStaffById(Long id);

    List<UserRequest> getAllFeeStaff();

    List<UserRequest> getActiveFeeStaff();

    UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto);

    @Transactional
    void inActiveNonTeachingStaff(Long id);
}
