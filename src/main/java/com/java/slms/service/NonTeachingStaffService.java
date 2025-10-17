package com.java.slms.service;

import com.java.slms.dto.UserRequest;
import com.java.slms.model.NonTeachingStaff;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NonTeachingStaffService
{
    // Create new Fee Staff
    UserRequest createFeeStaff(UserRequest feeStaffDto, Long schoolId);

    // Get Fee Staff by ID
    UserRequest getFeeStaffById(Long id, Long schoolId);

    // Get all Fee Staff in a school
    List<UserRequest> getAllFeeStaff(Long schoolId);

    // Get all active Fee Staff in a school
    List<UserRequest> getActiveFeeStaff(Long schoolId);

    // Update Fee Staff details
    UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto, Long schoolId);

    // Get Non-Teaching Staff by email and school ID
    NonTeachingStaff getNonTeachingStaffByEmailAndSchool(String email, Long schoolId);

    // Inactivate a Non-Teaching Staff
    @Transactional
    void inActiveNonTeachingStaff(Long id, Long schoolId);
}
