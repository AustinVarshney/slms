package com.java.slms.service;

import com.java.slms.model.Staff;

public interface StaffService
{
    Staff getStaffByEmailAndSchoolId(String email, Long schoolId);

}
