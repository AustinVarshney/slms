package com.java.slms.serviceImpl;

import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Staff;
import com.java.slms.repository.StaffRepository;
import com.java.slms.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService
{
    private final StaffRepository staffRepository;

    @Override
    public Staff getStaffByEmailAndSchoolId(String email, Long schoolId)
    {
        return staffRepository.findByEmailInCurrentSchool(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff not found with email: " + email + " in current school"));
    }
}
