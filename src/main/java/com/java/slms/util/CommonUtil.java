package com.java.slms.util;

import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil
{

    public static ClassEntity fetchClassEntityByClassId(ClassEntityRepository classEntityRepository, Long classId)
    {
        return classEntityRepository.findById(classId).orElseThrow(() ->
        {
            log.error("Class with ID '{}' not found.", classId);
            return new ResourceNotFoundException("Class not found with ID: " + classId);
        });
    }

    public static Student fetchStudentByPan(StudentRepository studentRepository, String pan)
    {
        return studentRepository.findById(pan)
                .orElseThrow(() ->
                {
                    log.error("Student with PAN number '{}' was not found.", pan);
                    return new ResourceNotFoundException("Student with PAN number '" + pan + "' was not found.");
                });

    }

    public static User fetchUserByUserId(UserRepository userRepository, Long userId)
    {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                {
                    log.error("User not found with Id: " + userId);
                    return new ResourceNotFoundException("User not found with Id: " + userId);
                });
    }

    public static Admin fetchAdminById(AdminRepository adminRepository, Long adminId)
    {
        return adminRepository.findById(adminId)
                .orElseThrow(() ->
                {
                    log.error("Admin not found with Id: " + adminId);
                    return new ResourceNotFoundException("Admin not found with Id: " + adminId);
                });
    }

    public static FeeStaff fetchFeeStaffById(FeeStaffRepository feeStaffRepository, Long feeStaffId)
    {
        return feeStaffRepository.findById(feeStaffId)
                .orElseThrow(() ->
                {
                    log.error("FeeStaff not found with Id: " + feeStaffId);
                    return new ResourceNotFoundException("FeeStaff not found with Id: " + feeStaffId);
                });
    }


}
